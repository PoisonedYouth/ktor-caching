package com.poisonedyouth.caching.adapter.persistence

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date

object UserTable : UUIDTable("user", "id") {
    val firstName = text("first_name")
    val lastName = text("last_name")
    val birthDate = date("birth_date")
    val address = reference("address_id", AddressTable)
}

object AddressTable : UUIDTable("address", "id") {
    val streetName = text("street_name")
    val streetNumber = text("street_number")
    val zipCode = integer("zip_code")
    val city = text("city")
}