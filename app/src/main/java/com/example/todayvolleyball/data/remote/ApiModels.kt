package com.example.todayvolleyball.data.remote

import com.google.gson.annotations.SerializedName

data class TeamResponse(
    @SerializedName("result") val result: ApiResult,
    @SerializedName("payload") val payload: List<TeamPayload>?
)

data class TeamPayload(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("tcode") val tcode: String?,
    @SerializedName("team") val team: TeamInfo?,
    @SerializedName("themes") val themes: List<TeamTheme>?
)

data class TeamTheme(
    @SerializedName("type") val type: String?,
    @SerializedName("primaryColor") val primaryColor: String?
)

data class TeamInfo(
    @SerializedName("code") val code: String?,
    @SerializedName("type") val type: String?, // MEN, WOMEN
    @SerializedName("gender") val gender: String?, // MALE, FEMALE
    @SerializedName("translations") val translations: List<TeamTranslation>?
)

data class TeamTranslation(
    @SerializedName("languageCode") val languageCode: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("shortName") val shortName: String?
)

data class ScheduleResponse(
    @SerializedName("result") val result: ApiResult,
    @SerializedName("payload") val payload: SchedulePayload?
)

data class SchedulePayload(
    @SerializedName("content") val content: List<ScheduleContent>?
)

data class ScheduleContent(
    @SerializedName("rank") val rank: Int,
    @SerializedName("seasonCode") val seasonCode: String?,
    @SerializedName("leagueCode") val leagueCode: String?,
    @SerializedName("gender") val gender: String?, // "1": Men, "2": Women
    @SerializedName("gnum") val gnum: Int,
    @SerializedName("hcode") val hcode: String?,
    @SerializedName("hname") val hname: String?,
    @SerializedName("hsname") val hsname: String?,
    @SerializedName("acode") val acode: String?,
    @SerializedName("aname") val aname: String?,
    @SerializedName("asname") val asname: String?,
    @SerializedName("gdate") val gdate: String?,
    @SerializedName("gstime") val gstime: String?,
    @SerializedName("hspoint") val hspoint: Int,
    @SerializedName("aspoint") val aspoint: Int,
    @SerializedName("place") val place: String?,
    @SerializedName("hs1point") val hs1point: Int,
    @SerializedName("hs2point") val hs2point: Int,
    @SerializedName("hs3point") val hs3point: Int,
    @SerializedName("hs4point") val hs4point: Int,
    @SerializedName("hs5point") val hs5point: Int,
    @SerializedName("as1point") val as1point: Int,
    @SerializedName("as2point") val as2point: Int,
    @SerializedName("as3point") val as3point: Int,
    @SerializedName("as4point") val as4point: Int,
    @SerializedName("as5point") val as5point: Int
)

data class ApiResult(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?
)
