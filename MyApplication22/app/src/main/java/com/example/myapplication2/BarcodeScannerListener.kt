package com.example.myapplication2


import com.example.myapplication2.ScanData

interface ScannerCallback {
    fun onScanReceived(scanData: ScanData)
}