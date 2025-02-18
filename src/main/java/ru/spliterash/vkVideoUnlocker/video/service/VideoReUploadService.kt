package ru.spliterash.vkVideoUnlocker.video.service

import jakarta.inject.Singleton
import kotlinx.coroutines.*
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.video.entity.VideoEntity
import ru.spliterash.vkVideoUnlocker.video.exceptions.SelfVideoException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoOpenException
import ru.spliterash.vkVideoUnlocker.video.holder.VideoContentHolder
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.repository.VideoRepository
import ru.spliterash.vkVideoUnlocker.video.service.dto.UnlockResult
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.util.*

@Singleton
class VideoReUploadService(
    private val videoService: VideoService,
    private val videoRepository: VideoRepository,
    @WorkUser private val workUser: VkApi,
    @GroupUser private val groupUser: VkApi,
) {
    private val inProgress = Collections.synchronizedMap(hashMapOf<String, Deferred<UnlockResult>>())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun getUnlockedId(holder: VideoContentHolder): UnlockResult {
        return inProgress.computeIfAbsent(holder.attachmentId) {
            scope.async {
                try {
                    actualGetUnlockedId(holder)
                } finally {
                    inProgress.remove(holder.attachmentId)
                }
            }
        }.await()
    }

    private suspend fun softGetUnlockedId(holder: VideoContentHolder): UnlockResult? {
        val attachmentId = holder.attachmentId
        val unlocked = videoRepository.findVideo(attachmentId)
        if (unlocked != null)
            return UnlockResult(unlocked.unlockedId, unlocked.private)
        val video = holder.video()
        checkForReUpload(video)

        // Проверяем на закрытость только видео
        if (holder is VideoHolder) {
            val locked = holder.isLocked()
            if (!locked)
                throw VideoOpenException()
        }
        return null
    }

    private suspend fun actualGetUnlockedId(holder: VideoContentHolder): UnlockResult {
        val unlockedId = softGetUnlockedId(holder)
        if (unlockedId != null) return unlockedId

        return reUploadAndSave(holder)
    }

    private fun checkForReUpload(video: VkVideo) {
        if (video.ownerId == -groupUser.id)
            throw SelfVideoException()
    }

    private suspend fun reUploadAndSave(holder: VideoContentHolder): UnlockResult {
        val fullVideo = holder.fullVideo()

        val originalAttachmentId = holder.attachmentId
        val videoAccessor = fullVideo.toAccessor()
        val private = fullVideo.shouldBeLocked()

        val reUploadedId = workUser.videos.upload(
            groupUser.id,
            originalAttachmentId,
            private,
            videoAccessor
        )

        val entity = VideoEntity(originalAttachmentId, reUploadedId, private)
        videoRepository.save(entity)

        return UnlockResult(reUploadedId, private)
    }
}