package com.microsoftTeams.bot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.schema.*;
import com.microsoftTeams.bot.helpers.MergeRequest;
import com.microsoftTeams.bot.helpers.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.time.*;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would
 * be added. For this sample, the {@link #onMessageActivity(TurnContext)} updates conversationReferences
 * {@link ConversationReferences}. The
 * {@link #onMembersAdded(List, TurnContext)} will send a greeting to new
 * conversation participants with instructions for sending a proactive message.
 * </p>
 */
public class GitlabBot extends ActivityHandler {
    @Value("${server.port:3978}")
    private int port;

    // Message to send to users when the bot receives a Conversation Update event
    private final String welcomeMessage =
        "Successfully added, we will notify about your operations in Gitlab.\n" + "\nThanks!!";

    private final String message = "Use the available commands.\n" +
            "\n 1) 'add reminder' (to avail daily notification service for the merge request in which you are a reviewer)\n" +
            "\n 2) 'remove reminder' (to remove daily notification service for the merge request in which you are a reviewer)";

    private final ConversationReferences conversationReferences;

    private final ReviewerUsers reviewerUsers;

    public GitlabBot(ConversationReferences withReferences, ReviewerUsers withUsers) {
        conversationReferences = withReferences;
        reviewerUsers = withUsers;
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());
        String text = turnContext.getActivity().getText().toLowerCase();
        if(text.equals("add reminder")){
            int index = reviewerUsers.getUsers().indexOf(turnContext.getActivity().getConversationReference().getUser().getId());
            if(index != -1){
                return turnContext.sendActivity(MessageFactory.text("Already added")).thenApply(sendResult -> null);
            }
            else{
                reviewerUsers.getUsers().add(turnContext.getActivity().getConversationReference().getUser().getId());
                return turnContext.sendActivity(MessageFactory.text("Successfully added")).thenApply(sendResult -> null);
            }

        }
        else if(text.equals("remove reminder")){
            int index = reviewerUsers.getUsers().indexOf(turnContext.getActivity().getConversationReference().getUser().getId());
            if(index != -1){
                reviewerUsers.getUsers().remove(turnContext.getActivity().getConversationReference().getUser().getId());
            }
            return turnContext.sendActivity(MessageFactory.text("Successfully removed")).thenApply(sendResult -> null);
        }
        return turnContext
                .sendActivity(MessageFactory.text(message))
                .thenApply(sendResult -> null);
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        return membersAdded.stream()
            .filter(
                // Greet anyone that was not the target (recipient) of this message.
                member -> !StringUtils
                    .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
            )
            .map(
                channel -> turnContext
                    .sendActivity(MessageFactory.text(String.format(welcomeMessage, port)))
            )
            .collect(CompletableFutures.toFutureList())
            .thenApply(resourceResponses -> null);
    }

    @Override
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());
        return super.onConversationUpdateActivity(turnContext);
    }

    // adds a ConversationReference to the shared Map.
    private void addConversationReference(Activity activity) {
        ConversationReference conversationReference = activity.getConversationReference();
        conversationReferences.put(conversationReference.getUser().getId(), conversationReference);
    }

    /**
     * fetch userDetails using the email from ms-teams bot by GitlabAPI
     * @param email
     * @param accessToken
     * @return
     */
    public static CompletableFuture<User> waitForUser(String email, String accessToken){
        email = "avinash.ranjan@sprinklr.com";
        String url = "https://gitlab.com/api/v4/users?search=" + email;
        return fetchUser(url, accessToken);
    }

    /**
     * fetching list of mergeRequest in which reviewer is user by GitlabAPI
     * @param username
     * @param userId
     * @param accessToken
     * @return
     */
    public static CompletableFuture<List<MergeRequest>> waitForMergeRequest(String username,String userId, String accessToken) {
        String url = "https://gitlab.com/api/v4/merge_requests?scope=all&state=opened&reviewer_username=" + username;

        // if we need to send all the merge requests which is already approved by the user
        if(!userId.isEmpty()){
            url = url + "&approved_by_ids[]=" + userId;
        }
        return fetchMergeRequest(url, accessToken);
    }

    /**
     * utility function to send http request to the Gitlab endpoint to fetch user details
     * @param url
     * @param accessToken
     * @return
     */
    private static CompletableFuture<User> fetchUser(String url, String accessToken){
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("PRIVATE-TOKEN", accessToken);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    // Read the response into a JsonNode
                    JsonNode rootNode = objectMapper.readTree(connection.getInputStream());

                    // Check if the root node is an array
                    if (rootNode.isArray() && !rootNode.isEmpty()) {
                        // Get the first element from the array
                        JsonNode userNode = rootNode.get(0);
                        // Deserialize the JSON object into a MergeRequest object
                        User user = objectMapper.treeToValue(userNode, User.class);
                        return user;
                    } else {
                        return null;
                    }
                } else {
                    System.err.println("Error: " + connection.getResponseMessage());
                    return null; // Return null if there was an error
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null; // Return null in case of exception
            }
        });
    }

    /**
     * utility function to fetch list of merge request by sending http request to Gitlab endpoint
     * @param url
     * @param accessToken
     * @return
     */
    private static CompletableFuture<List<MergeRequest>> fetchMergeRequest(String url, String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("PRIVATE-TOKEN", accessToken);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    // Read the response into a JsonNode
                    JsonNode rootNode = objectMapper.readTree(connection.getInputStream());

                    // Check if the root node is an array
                    if (rootNode.isArray() && !rootNode.isEmpty()) {
                        List<MergeRequest> mergeRequests = new ArrayList<>();
                        for(JsonNode mergeRequestNode : rootNode){
                            // Deserialize the JSON object into a MergeRequest object
                            MergeRequest mergeRequest = objectMapper.treeToValue(mergeRequestNode, MergeRequest.class);
                            mergeRequests.add(mergeRequest);
                        }
                        return mergeRequests;
                    } else {
                        return null;
                    }
                } else {
                    System.err.println("Error: " + connection.getResponseMessage());
                    return null; // Return null if there was an error
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null; // Return null in case of exception
            }
        });
    }


}
