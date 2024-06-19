package com.microsoftTeams.bot;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.ActionTypes;
import com.microsoft.bot.schema.CardAction;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.HeroCard;
import com.microsoftTeams.bot.helpers.Author;
import com.microsoftTeams.bot.helpers.Builds;
import com.microsoftTeams.bot.helpers.MergeRequest;
import com.microsoftTeams.bot.helpers.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * This controller will receive GET requests at /api/notify and send a message
 * to all ConversationReferences.
 *
 * @see ConversationReferences
 * @see GitlabBot
 * @see Application
 */
@RestController
public class NotifyController {
    /**
     * The BotFrameworkHttpAdapter to use. Note is provided by dependency
     * injection via the constructor.
     *
     * @see com.microsoft.bot.integration.spring.BotDependencyConfiguration
     */
    private final BotFrameworkHttpAdapter adapter;

    // which stores author who creates PR, comments and initiate pipeline
    private final Map<Long, Author> dictionary;

    private final ConversationReferences conversationReferences;

    private final ReviewerUsers reviewerUsers;

    private final String appId;

    @Value("${accessToken}")
    private String accessToken;

    @Autowired
    public NotifyController(
        BotFrameworkHttpAdapter withAdapter,
        Configuration withConfiguration,
        ConversationReferences withReferences,
        ReviewerUsers withUsers
    ) {
        adapter = withAdapter;
        conversationReferences = withReferences;
        appId = withConfiguration.getProperty("MicrosoftAppId");
        reviewerUsers = withUsers;
        this.dictionary = new ConcurrentHashMap<>();
    }

    @PostMapping("/api/Gitlab/buildNotify")
    public void pipelinePulLRequestNotification(@RequestBody Information information){
        HeroCard heroCard = new HeroCard();

        // if the event is related to create, update and merge status of PR in gitlab
        switch (information.getObjectKind()) {
            case "merge_request":

                // When first time PR is created then we store author with the id in dictionary
                dictionary.computeIfAbsent(information.getObjectAttributes().getId(), k -> information.getObjectAttributes().getLastCommit().getAuthor());

                // Notify the author when PR is merged
                Author authorMergeRequest = dictionary.get(information.getObjectAttributes().getId());
                switch (information.getObjectAttributes().getState()) {
                    case "merged":
                        heroCard.setTitle("Pull Request Merged");
                        heroCard.setSubtitle("Hii, " + authorMergeRequest.getName());
                        heroCard.setText("Your pull request titled '" + information.getObjectAttributes().getTitle() + "' for the project '" + information.getProject().getName()  + "' has been merged by " + information.getUser().getName() + ".");
                        heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View PR", information.getObjectAttributes().getUrl()));

                        // send notification only to the first user for the demonstration purpose
                        if (!conversationReferences.isEmpty()) {
                            ConversationReference reference = conversationReferences.entrySet().iterator().next().getValue();
                            adapter.continueConversation(
                                    appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                            );
                        } else {
                            System.out.println("Empty");
                        }
                        break;
                    // notify user when PR is closed
                    case "closed":
                        heroCard.setTitle("Pull Request Closed");
                        heroCard.setSubtitle("Hii, " + authorMergeRequest.getName());
                        heroCard.setText("Your pull request titled '" + information.getObjectAttributes().getTitle() + "' for the project '" + information.getProject().getName()  + "' has been closed by " + information.getUser().getName() + ".");
                        heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View PR", information.getObjectAttributes().getUrl()));

                        // send notification only to the first user for the demonstration purpose
                        if (!conversationReferences.isEmpty()) {
                            ConversationReference reference = conversationReferences.entrySet().iterator().next().getValue();
                            adapter.continueConversation(
                                    appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                            );
                        } else {
                            System.out.println("Empty");
                        }
                        break;
                    // notify user when some other user commit in the PR
                    case "opened":
                        heroCard.setTitle("Commit added");
                        heroCard.setSubtitle("Hii, " + authorMergeRequest.getName());
                        heroCard.setText("A new commit titled \"" + information.getObjectAttributes().getTitle() + "\" by " + information.getUser().getName() + " has been added to your pull request on the \"" + information.getProject().getName() + "\" project, with the message \"" + information.getObjectAttributes().getLastCommit().getMessage() + "\".");
                        heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Commit", information.getObjectAttributes().getUrl()));

                        // send notification only to the first user for the demonstration purpose
                        if (!conversationReferences.isEmpty()) {
                            ConversationReference reference = conversationReferences.entrySet().iterator().next().getValue();
                            adapter.continueConversation(
                                    appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                            );
                        } else {
                            System.out.println("Empty");
                        }
                        break;
                }
                break;
            // if the event type is comment
            case "note":
                Author commentAuthor = dictionary.get(information.getObjectAttributes().getNoteableId());
                // when comment is made on the PR notify the author of the PR
                if (information.getObjectAttributes().getNoteableType().equals("MergeRequest")) {
                    heroCard.setTitle("Comment added");
                    heroCard.setSubtitle("Hii, " + commentAuthor.getName());
                    heroCard.setText("New comments have been received on your pull request for the '" + information.getProject().getName()  + "' project, titled '" + information.getMergeRequest().getTitle() + "' by " + information.getUser().getName() + "\n" + "\nComment : " + information.getObjectAttributes().getNote());
                    heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Comment", information.getObjectAttributes().getUrl()));

                    // send notification only to the first user for the demonstration purpose
                    if (!conversationReferences.isEmpty()) {
                        ConversationReference reference = conversationReferences.entrySet().iterator().next().getValue();
                        adapter.continueConversation(
                                appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                        );
                    } else {
                        System.out.println("Empty");
                    }
                }

                break;
            // if the event is related to the pipeline
            case "pipeline":
                Author pipelineAuthor = information.getCommit().getAuthor();
                // notify user when pipeline is failed
                switch (information.getObjectAttributes().getStatus()) {
                    case "failed":
                        for (Builds build : information.getBuilds()) {
                            if (build.getStatus().equals("failed")) {
                                heroCard.setTitle("Pipeline Status");
                                heroCard.setSubtitle("Hii, " + pipelineAuthor.getName());
                                heroCard.setText("The pipeline for the project \""+ information.getProject().getName() + "\" on the \"" + information.getObjectAttributes().getRef()+ "\" branch failed during the pipeline stage \"" + build.getStage() + "\".");
                                heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Pipeline", information.getObjectAttributes().getUrl()));

                                // send notification only to the first user for the demonstration purpose
                                if (!conversationReferences.isEmpty()) {
                                    ConversationReference reference = conversationReferences.entrySet().iterator().next().getValue();
                                    adapter.continueConversation(
                                            appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                                    );
                                } else {
                                    System.out.println("Empty");
                                }
                                break;
                            }
                        }
                        break;
                    case "success":
                        heroCard.setTitle("Pipeline Status");
                        heroCard.setSubtitle("Hii, " + pipelineAuthor.getName());
                        heroCard.setText("The pipeline for the project \""+ information.getProject().getName() + "\" on the \"" + information.getObjectAttributes().getRef()+ "\" branch has successfully completed.");
                        heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Pipeline", information.getObjectAttributes().getUrl()));

                        // send notification only to the first user for the demonstration purpose
                        if (!conversationReferences.isEmpty()) {
                            ConversationReference reference = conversationReferences.entrySet().iterator().next().getValue();
                            adapter.continueConversation(
                                    appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                            );
                        } else {
                            System.out.println("Empty");
                        }
                        break;
                }
                break;
        }
    }


    /**
     * Triggers by cron job at specific time to send notification regarding reviews needed list
     */
    @GetMapping("/api/Gitlab/reviewNotification")
    public void sendReviewNotification(){

        // sending notification to all the users who want review list
        for(String user : reviewerUsers.getUsers()){

            // Gitlab api to get userDetails using email of the user in ms-teams
            CompletableFuture<User> userFuture = GitlabBot.waitForUser(user, accessToken);
            AtomicReference<User> localUser = new AtomicReference<>(null); // Use AtomicReference
            CompletableFuture<Void> resultHandler = userFuture.thenAccept(users -> {
                if (users != null) {
                    // Save the users to the AtomicReference
                    localUser.set(users);
                } else {
                    System.out.println("Failed to receive User");
                }
            });

            // wait for the resultHandler to ensure that the user handling is complete
            resultHandler.join();

            // use the localUser.get() to retrieve the user
            User users = localUser.get();


            // get all merge request in which user is the reviewer
            CompletableFuture<List<MergeRequest>> mergeRequestFuture = GitlabBot.waitForMergeRequest(users.getUsername(), "", accessToken);
            AtomicReference<List<MergeRequest>> localMergeRequest = new AtomicReference<>(null);
            localMergeRequest.set(new ArrayList<>());
            mergeRequestFuture.thenAccept(mergeRequests -> {
                if(mergeRequests != null && !mergeRequests.isEmpty()){
                    for(MergeRequest mergeRequest: mergeRequests){
                        localMergeRequest.get().add(mergeRequest);
                    }
                }else{
                    System.out.println("Failed to receive merge requests");
                }
            }).join();
            List<MergeRequest> mergeRequestAll = localMergeRequest.get();

            // get all merge request in which user is reviewer and already approved
            mergeRequestFuture = GitlabBot.waitForMergeRequest(users.getUsername(), users.getId(), accessToken);
            localMergeRequest.set(new ArrayList<>());
            mergeRequestFuture.thenAccept(mergeRequests -> {
                if(mergeRequests != null && !mergeRequests.isEmpty()){
                    for(MergeRequest mergeRequest: mergeRequests){
                        localMergeRequest.get().add(mergeRequest);
                    }
                }else{
                    System.out.println("Failed to receive merge requests");
                }
            }).join();

            // ids of all the mergeRequest which is already approved
            List<Long> ids = localMergeRequest.get().stream()
                                .map(MergeRequest::getId)
                                .collect(Collectors.toList());

            // Filter mergeRequestAll such that it doesn't contain mergeRequestId which is already approved by user
            List<MergeRequest> result = mergeRequestAll.stream()
                    .filter(mr -> !ids.contains(mr.getId()))
                    .collect(Collectors.toList());

            // reverse such that old PRs get more priority
            Collections.reverse(result);

            // format message to send notification and contains max 5 reviews mergeRequest
            String message = formatResponse(result);

            // if there is PRs which requires review from the user
            if(!message.isEmpty()){

                // fetch conversationReference of that user to send review list
                ConversationReference reference = conversationReferences.get(user);
                HeroCard heroCard = new HeroCard();
                heroCard.setTitle("Reviews Needed");
                heroCard.setText(message);
                adapter.continueConversation(
                        appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                );
            }
        }
    }

    /**
     * used for formatting the mergeRequest list to send notification (at max 5 reviews list)
     * @param mergeRequests
     * @return
     */
    private String formatResponse(List<MergeRequest> mergeRequests){
        StringBuilder sb = new StringBuilder();
        if(mergeRequests.isEmpty())
            return "";
        sb.append("List of pull requests which requires your review:\n");
        for(int i = 0; i < Math.min(mergeRequests.size(), 5); i++){
            sb.append(i + 1).append(") Pull request by ").append(mergeRequests.get(i).getAuthor().getName()).append("\n").append(mergeRequests.get(i).getWebUrl()).append("\n \n");
        }
        return sb.toString();
    }

}
