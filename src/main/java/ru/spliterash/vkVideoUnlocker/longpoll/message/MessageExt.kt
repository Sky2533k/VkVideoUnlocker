package ru.spliterash.vkVideoUnlocker.longpoll.message

import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

suspend inline fun Message.reply(client: VkApi, text: String?, attachments: String? = null) =
    client.messages.sendMessage(peerId, text, conversationMessageId, attachments)
