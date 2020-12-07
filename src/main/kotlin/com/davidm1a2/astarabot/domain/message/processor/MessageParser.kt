package com.davidm1a2.astarabot.domain.message.processor

import com.davidm1a2.astarabot.domain.AccountUUIDFinder
import com.davidm1a2.astarabot.domain.message.data.Message
import com.davidm1a2.astarabot.domain.message.data.MessageType

class MessageParser {
    private val accountFinder = AccountUUIDFinder()

    private val loginFormat = Regex("([a-zA-Z_0-9]{1,16}) joined the game")
    private val logoutFormat = Regex("([a-zA-Z_0-9]{1,16}) left the game")
    private val messageFormat = Regex("<([a-zA-Z_0-9]{1,16})> (.*)")
    private val pmFormat = Regex("<--([a-zA-Z_0-9]{1,16}): (.*)")

    fun parse(rawMessage: String): Message {
        val messageMatch = messageFormat.matchEntire(rawMessage)
        if (messageMatch != null) {
            val account = accountFinder.getAccount(messageMatch.groups[1]?.value)
            val message = messageMatch.groups[2]?.value
            return if (account != null && message != null) {
                Message(MessageType.CHAT, message, account)
            } else {
                Message(MessageType.UNKNOWN, rawMessage)
            }
        }

        val pmMatch = pmFormat.matchEntire(rawMessage)
        if (pmMatch != null) {
            val account = accountFinder.getAccount(pmMatch.groups[1]?.value)
            val message = pmMatch.groups[2]?.value
            return if (account != null && message != null) {
                Message(MessageType.PM, message, account)
            } else {
                Message(MessageType.UNKNOWN, rawMessage)
            }
        }

        val loginMatch = loginFormat.matchEntire(rawMessage)
        if (loginMatch != null) {
            val account = accountFinder.getAccount(loginMatch.groups[1]?.value)
            return if (account != null) {
                Message(MessageType.LOGIN, "", account)
            } else {
                Message(MessageType.UNKNOWN, rawMessage)
            }
        }

        val logoutMatch = logoutFormat.matchEntire(rawMessage)
        if (logoutMatch != null) {
            val account = accountFinder.getAccount(logoutMatch.groups[1]?.value)
            return if (account != null) {
                Message(MessageType.LOGOUT, "", account)
            } else {
                Message(MessageType.UNKNOWN, rawMessage)
            }
        }

        return Message(MessageType.UNKNOWN, rawMessage)
    }
}