package com.martin.chatapp.models

import java.util.*

data class Rate (
        val text: String,
        val rate: Float,
        val createdAt: Date,
        val profileUmgURL: String = ""
)