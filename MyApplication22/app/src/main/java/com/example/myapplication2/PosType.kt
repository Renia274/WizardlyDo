package com.example.myapplication2

sealed class PosType {
    object Master : PosType()
    object Slave : PosType()
}