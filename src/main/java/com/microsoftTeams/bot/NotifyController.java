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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final String appId;

    @Autowired
    public NotifyController(
        BotFrameworkHttpAdapter withAdapter,
        Configuration withConfiguration,
        ConversationReferences withReferences
    ) {
        adapter = withAdapter;
        conversationReferences = withReferences;
        appId = withConfiguration.getProperty("MicrosoftAppId");
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
}
