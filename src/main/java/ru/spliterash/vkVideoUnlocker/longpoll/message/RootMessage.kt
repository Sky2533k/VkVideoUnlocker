package ru.spliterash.vkVideoUnlocker.longpoll.message

import com.fasterxml.jackson.annotation.JsonProperty

data class RootMessage(
    @JsonProperty("attachments") override val attachments: List<Attachment>,
    @JsonProperty("conversation_message_id") val conversationMessageId: Long,
    @JsonProperty("fwd_messages") override val fwdMessages: List<FwdMessage> = listOf(),
    @JsonProperty("peer_id") val peerId: Long,
    @JsonProperty("reply_message") override val replyMessage: FwdMessage?,
    @JsonProperty("text") override val text: String?
) : Message