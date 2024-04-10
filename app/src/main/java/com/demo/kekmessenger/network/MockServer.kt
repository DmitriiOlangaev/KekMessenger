package com.demo.kekmessenger.network

import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MockServer @Inject constructor(private val parser: Parser) : ServerDao {
    private val TAG = "MockServer"
    private val seed = 1337
    private val random = Random(seed)
    private val channelsCount = 50
    private val messageCount = 2000
    private var id = 1
    private val channels: MutableList<String> = mutableListOf()
    private val time = 1712589425839
    private val messages =
        Array<MutableList<DataClassesForParser.JsonMessage>>(channelsCount) { mutableListOf() }
    private val images = listOf(
        "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCzcKh53jQ-J92rn3URTBb6fNeoLuPwkV9whonkJHa9Q&s",
        "https://content.imageresizer.com/images/memes/Patrick-Bateman-listening-to-music-meme-7.jpg",
        "https://w7.pngwing.com/pngs/45/1019/png-transparent-christian-bale-patrick-bateman-meme-memes-mixed-memes-thumbnail.png",
        "https://content.imageresizer.com/images/memes/Patrick-Bateman-meme-4.jpg",
        "https://images.thewest.com.au/publication/C-12334361/1999e8fcd4200f4ddca47b14c8ec7616ce3e4a51-16x9-x138y0w3097h1742.jpg",
        "https://static.wikia.nocookie.net/your-bizarre-adventure/images/1/12/Patrick_Bateman.png/revision/latest?cb=20220426034138",
        "https://smeshariki-mir.ru/oficial/cs_001kr.jpg",
        "https://www.b17.ru/foto/article/274804.jpg",
        "https://i.ytimg.com/vi/x5a2Sc5nIIU/hqdefault.jpg",
        "https://avatars.dzeninfra.ru/get-zen_doc/40274/pub_5f16e069fb16df3a46229203_5f16e25881dacd7c88445bdd/scale_1200",
        "https://citaty.info/files/quote-pictures/27378-boicovskii-klub-fight-club.jpg",
        "https://decider.com/wp-content/uploads/2022/01/MEAT-LOAF-FIGHT-CLUB.jpg",
        "https://www.soyuz.ru/public/uploads/files/5/7625303/1005x558_202306301354359c6aaf0358.jpg",
        "https://cdn27.echosevera.ru/64809353eac9120dd845a103/6484502b61cba.jpg",
        "https://variety.com/wp-content/uploads/2023/07/rev-1-BAR-TT3-0104_High_Res_JPEG-e1689894209136.jpeg?w=1000&h=563&crop=1",
        "https://cs8.pikabu.ru/post_img/2018/03/24/8/1521898464135567246.jpg"
    )

    init {
        generateChannels()
        generateMessages()
    }

    private fun channelName(i: Int): String = "channel$i"

    private fun generateChannels() {
        repeat(channelsCount) {
            channels.add(channelName(it))
        }
    }

    private fun generateMessages() {
        repeat(channelsCount) { channel ->
            repeat(messageCount) {
                messages[channel].add(generateMessage(channelName(channel)))
            }

        }
    }

    private fun generateMessage(channel: String): DataClassesForParser.JsonMessage {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val rndInt = random.nextInt()
        val jsonMessageText = if (rndInt % 2 != 0) DataClassesForParser.JsonMessageText(
            "Text#" +
                    (1..random.nextInt(1, 30)).map { allowedChars.random(random) }
                        .joinToString("")
        ) else null
        val jsonMessageImage = if (rndInt % 2 == 0) DataClassesForParser.JsonMessageImage(
            images[random.nextInt(
                0,
                images.size
            )]
        ) else null
        return DataClassesForParser.JsonMessage(
            id++,
            "Name#" + (1..random.nextInt(1, 5)).map { allowedChars.random(random) }
                .joinToString(""),
            channel,
            DataClassesForParser.JsonMessageData(
                jsonMessageText, jsonMessageImage
            ),
            time + id
        )
    }


    override suspend fun postMessage(requestBody: RequestBody): Response<ResponseBody> {
        return Response.error(500, "".toResponseBody("application/json".toMediaType()))
    }

    override suspend fun uploadImage(
        jsonPart: MultipartBody.Part,
        imagePart: MultipartBody.Part
    ): Response<ResponseBody> {
        return Response.error(
            500,
            "".toResponseBody("application/json".toMediaType())
        )
    }

    override suspend fun getChannels(): Response<ResponseBody> {
        return Response.success(
            200,
            parser.toJson(channels.toList(), object : ParameterizedType {
                override fun getActualTypeArguments(): Array<Type> {
                    return arrayOf(String::class.java)
                }

                override fun getRawType(): Type {
                    return List::class.java
                }

                override fun getOwnerType(): Type? {
                    return null
                }

            })
                .toResponseBody("application/json".toMediaType())
        )
    }

    override suspend fun getMessages(
        channel: String,
        lastKnownId: Int,
        limit: Int,
        reverse: Boolean
    ): Response<ResponseBody> {
        delay(200)
        val channelInt = channel.removePrefix("channel").toInt()
        val channelMessages = messages[channelInt]
        var l: Int
        var r: Int
        var ind =
            if (reverse) channelMessages.indexOfFirst { it.id >= lastKnownId }
            else channelMessages.indexOfFirst { it.id > lastKnownId }
        if (reverse) {
            l = ind - limit
            r = ind
        } else {
            if (ind == -1) {
                ind = channelMessages.size
            }
            l = ind
            r = l + limit
        }
        l = max(l, 0)
        r = min(r, channelMessages.size)
        val mess = channelMessages.subList(
            l, r
        )
        return Response.success(
            200,
            parser.toJson(mess, object : ParameterizedType {
                override fun getActualTypeArguments(): Array<Type> {
                    return arrayOf(DataClassesForParser.JsonMessage::class.java)
                }

                override fun getRawType(): Type {
                    return List::class.java
                }

                override fun getOwnerType(): Type? {
                    return null
                }

            })
                .toResponseBody("application/json".toMediaType())
        )
    }
}