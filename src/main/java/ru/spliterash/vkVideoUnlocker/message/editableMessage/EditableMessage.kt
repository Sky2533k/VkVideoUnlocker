package ru.spliterash.vkVideoUnlocker.message.editableMessage

interface EditableMessage {
    suspend fun sendOrUpdate(text: String?, attachments: String? = null)
}