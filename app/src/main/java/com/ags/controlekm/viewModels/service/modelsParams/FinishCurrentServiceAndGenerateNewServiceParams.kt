package com.ags.controlekm.viewModels.service.modelsParams

data class FinishCurrentServiceAndGenerateNewServiceParams(
    var departureAddress: String,
    var serviceAddress: String,
    var departureKm: Int,
    var date: String,
    var time: String,
    var serviceSummary: String,
)
