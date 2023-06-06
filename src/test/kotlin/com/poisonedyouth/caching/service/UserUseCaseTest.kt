package com.poisonedyouth.caching.service

import arrow.core.left
import com.poisonedyouth.caching.adapter.persistence.AddressTable
import com.poisonedyouth.caching.adapter.persistence.ExposedUserRepository
import com.poisonedyouth.caching.adapter.persistence.UserTable
import com.poisonedyouth.caching.failure.Failure
import com.poisonedyouth.caching.model.UUIDIdentity
import com.poisonedyouth.caching.port.UserRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.util.UUID


@Testcontainers
class UserUseCaseTest {

    private val userRepository: UserRepository = spy(ExposedUserRepository())
    private val userUseCase = UserUseCase(userRepository)

    @Container
    val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14.4-alpine")
        .withExtraHost("localhost", "127.0.0.1")
        .withExposedPorts(5432)
        .withDatabaseName("db")
        .withUsername("root")
        .withPassword("password")


    @BeforeEach
    fun setUp() {
        postgreSQLContainer.start()
        val database = Database.connect(
            url = postgreSQLContainer.jdbcUrl,
            driver = postgreSQLContainer.driverClassName,
            user = postgreSQLContainer.username,
            password = postgreSQLContainer.password
        )
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(AddressTable, UserTable)
        }
    }

    @Test
    fun `addNewUser throws exception if user already exists`() = runBlocking{
        // given
        val id = UUIDIdentity(UUID.randomUUID())
        doReturn(
            UserDto(
                id = id.id.toString(),
                firstName = "John",
                lastName = "Doe",
                birthDate = LocalDate.of(2000, 1, 1),
                address = AddressDto(
                    id = id.id.toString(),
                    streetName = "Main Street",
                    streetNumber = "122",
                    zipCode = 22222,
                    city = "Los Angeles"
                )
            ).toUser()
        ).whenever(userRepository).findBy(any())

        val user = UserDto(
            id = id.id.toString(),
            firstName = "Joe",
            lastName = "Black",
            birthDate = LocalDate.of(1999, 1, 1),
            address = AddressDto(
                id = id.id.toString(),
                streetName = "Main Street",
                streetNumber = "122",
                zipCode = 22222,
                city = "Los Angeles"
            )
        )

        // when
        val actual = userUseCase.addNewUser(user)

        // then
        assertThat(actual.leftOrNull()?.message)
            .isEqualTo("User with id '${id.id}' already exists.")
    }

    @Test
    fun `addNewUser throws exception if loading of user fails`() = runBlocking {
        // given
        val id = UUIDIdentity(UUID.randomUUID())
        whenever(userRepository.findBy(id)).thenAnswer {
            Failure.ValidationFailure("Failed!").left()
        }

        val user = UserDto(
            id = id.id.toString(),
            firstName = "Joe",
            lastName = "Black",
            birthDate = LocalDate.of(1999, 1, 1),
            address = AddressDto(
                id = UUID.randomUUID().toString(),
                streetName = "Main Street",
                streetNumber = "122",
                zipCode = 22222,
                city = "Los Angeles"
            )
        )

        // when
        val actual = userUseCase.addNewUser(user)

        // then
        assertThat(actual.leftOrNull()?.message).isEqualTo("Failed!")
    }
}
