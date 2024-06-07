package com.microsoftTeams.bot;

import com.microsoft.bot.schema.ConversationReference;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A Map of ConversationReference object the bot handling.
 *
 * @see NotifyController
 * @see GitlabBot
 */
public class ConversationReferences extends ConcurrentHashMap<String, ConversationReference> {
}
