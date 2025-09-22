package com.example.precastmanagementapp

import kotlinx.coroutines.flow.MutableSharedFlow

interface TagIdReceiveManager {

    val data: MutableSharedFlow<Resource<TagIdResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()

}