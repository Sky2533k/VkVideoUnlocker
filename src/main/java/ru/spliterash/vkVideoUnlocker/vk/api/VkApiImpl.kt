package ru.spliterash.vkVideoUnlocker.vk.api

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import okhttp3.CookieJar
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.group.api.Groups
import ru.spliterash.vkVideoUnlocker.message.Messages
import ru.spliterash.vkVideoUnlocker.video.api.Videos
import ru.spliterash.vkVideoUnlocker.vk.actor.types.Actor
import ru.spliterash.vkVideoUnlocker.vk.okhttp.VkInterceptor
import java.util.concurrent.TimeUnit

@Prototype
class VkApiImpl(
    @Parameter private val actor: Actor,
    factory: OkHttpFactory,
    context: ApplicationContext
) : VkApi {
    private val client = factory.create()
        .cookieJar(CookieJar.NO_COOKIES)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(VkInterceptor(actor))
        .build()
    override val id: Int
        get() = actor.id

    override val videos: Videos by lazy { context.createBean(Videos::class.java, client, actor) }
    override val messages: Messages by lazy { context.createBean(Messages::class.java, client, actor) }
    override val groups: Groups by lazy { context.createBean(Groups::class.java, client, actor) }
}