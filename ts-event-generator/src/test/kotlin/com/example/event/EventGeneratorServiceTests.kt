package com.example.event

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.powermock.reflect.Whitebox
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.util.ReflectionTestUtils
import java.time.Duration


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = ["app.scheduling.enable=false"])
internal class EventGeneratorServiceTests {

    private lateinit var baseUrl: String

    @SpyBean
    private lateinit var generatorService: EventGeneratorService

    private var server: MockWebServer = MockWebServer()

    @BeforeEach
    fun setUp() {
        server.start()
        baseUrl = String.format(
            "http://localhost:%s/event",
            server.port
        )

    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendImpressionEvent_CheckParameters() {

        ReflectionTestUtils.setField(generatorService, "impressionEventUrl", "$baseUrl/impression")
        ReflectionTestUtils.setField(generatorService, "clickEventUrl", "$baseUrl/click")
        val response = MockResponse().addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .setBody("impression event received")
        server.enqueue(response)
        val id: String = Whitebox.invokeMethod(generatorService, "sendImpressionEvent")
        val request = server.takeRequest()
        assertThat(id).isNotNull.isNotEmpty.isNotBlank.matches("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}")
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.path).startsWith("/event/impression")
    }

    @Test
    fun sendClickEvent_after10SecondsOfMethodCall() {

        ReflectionTestUtils.setField(generatorService, "impressionEventUrl", "$baseUrl/impression")
        ReflectionTestUtils.setField(generatorService, "clickEventUrl", "$baseUrl/click")
        ReflectionTestUtils.setField(generatorService, "clickEventRandomNumber", "0")
        val response = MockResponse().addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .setBody("click event received")
        server.enqueue(response)
        val id: String? = ReflectionTestUtils.invokeMethod(generatorService, "sendClickEvent", "test_request_id")
        val request = server.takeRequest()
        assertThat(id).isNull()
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted {
                assertThat(request.method).isEqualTo("POST")
                assertThat(request.path).startsWith("/event/click")

            }


    }


}