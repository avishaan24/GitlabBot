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

    private ConversationReferences conversationReferences;
    private String appId;

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
    public void allUsersOverridden(@RequestBody Information information){
        System.out.println(information);
        HeroCard heroCard = new HeroCard();

        // if the event is related to create, update and merge status of PR in gitlab
        if(information.getObject_kind().equals("merge_request")){

            // When first time PR is created then we store author with the id in dictionary
            if(dictionary.get(information.getObject_attributes().getId()) == null){
                dictionary.put(information.getObject_attributes().getId(), information.getObject_attributes().getLast_commit().getAuthor());
            }
            Author lastCommitAuthor = information.getObject_attributes().getLast_commit().getAuthor();
            Author author = dictionary.get(information.getObject_attributes().getId());

            // Notify the author when PR is merged
            if(information.getObject_attributes().getState().equals("merged")){
                heroCard.setTitle("Pull Request Merged");
                heroCard.setSubtitle("Hii, " + author.getName());
                heroCard.setText("Your PR on project "+ information.getProject().getName() + " with title " + information.getObject_attributes().getTitle() + " is now merged by " + information.getUser().getName());
                heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View PR", information.getObject_attributes().getUrl()));
            }
            // notify user when PR is closed
            else if(information.getObject_attributes().getState().equals("closed")){
                heroCard.setTitle("Pull Request Closed");
                heroCard.setSubtitle("Hii, " + author.getName());
                heroCard.setText("Your PR on project "+ information.getProject().getName() + " with title " + information.getObject_attributes().getTitle() + " is now closed by " + information.getUser().getName());
                heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View PR", information.getObject_attributes().getUrl()));
            }
            // notify user when some other user commit in the PR
            else if(information.getObject_attributes().getState().equals("opened")){
                heroCard.setTitle("Commits added");
                heroCard.setSubtitle("Hii, " + author.getName());
                heroCard.setText("New commit added on your PR on project "+ information.getProject().getName() + " with title " + information.getObject_attributes().getTitle() + " by " + information.getObject_attributes().getLast_commit().getAuthor().getName() + " with message " + information.getObject_attributes().getLast_commit().getMessage());
                heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View PR", information.getObject_attributes().getUrl()));
            }
        }
        // if the event type is comment
        else if(information.getObject_kind().equals("note")){
            // when comment is made on the PR notify the author of the PR
            if(information.getObject_attributes().getNoteable_type().equals("MergeRequest")){
                Author author = dictionary.get(information.getObject_attributes().getNoteable_id());
                heroCard.setTitle("Comments added");
                heroCard.setSubtitle("Hii, " + author.getName());
                heroCard.setText("New comments received on your PR on project "+ information.getProject().getName() + " with title " + information.getMerge_request().getTitle() + " by " + information.getUser().getName() +"\n" + "\nComments : " + information.getObject_attributes().getNote());
                heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View PR", information.getObject_attributes().getUrl()));
            }
        }
        // if the event is related to the pipeline
        else if(information.getObject_kind().equals("pipeline")){
            // notify user when pipeline is failed
            if(information.getObject_attributes().getStatus().equals("failed")){
                Author author = information.getCommit().getAuthor();
                for(Builds build: information.getBuilds()){
                    if(build.getStatus().equals("failed")){
                        heroCard.setTitle("Pipeline Status");
                        heroCard.setSubtitle("Hii, " + author.getName());
                        heroCard.setText("Your pipeline on project " + information.getProject().getName() + " at stage " + build.getStage() + " with name " + build.getName() + " got failed");
                        heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Pipeline", information.getObject_attributes().getUrl()));
                        break;
                    }
                }
            }
            else if(information.getObject_attributes().getStatus().equals("success")){
                Author author = information.getCommit().getAuthor();
                heroCard.setTitle("Pipeline Status");
                heroCard.setSubtitle("Hii, " + author.getName());
                heroCard.setText("Your pipeline on project " + information.getProject().getName()  + " got success");
                heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Pipeline", information.getObject_attributes().getUrl()));
            }
        }

        // send notification only to the first user for the demonstration purpose
        if(!conversationReferences.isEmpty()) {
            ConversationReference reference = conversationReferences.entrySet().iterator().next().getValue();
            adapter.continueConversation(
                    appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
            );
        }
        else{
            System.out.println("Empty");
        }
    }
}
