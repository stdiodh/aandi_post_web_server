package com.example.aandi_post_web_server.report.service

import com.example.aandi_post_web_server.report.dtos.ReportDetailDTO
import com.example.aandi_post_web_server.report.dtos.ReportRequestDTO
import com.example.aandi_post_web_server.report.dtos.ReportSummaryDTO
import com.example.aandi_post_web_server.report.entity.Report
import com.example.aandi_post_web_server.report.repository.ReportRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZoneId

@Service
class ReportService(
    private val reportRepository: ReportRepository,
) {
    suspend fun createReport(reportRequestDTO: ReportRequestDTO): Mono<Report> {
        val report = Report(
            week = reportRequestDTO.week,
            seq = reportRequestDTO.seq,
            title = reportRequestDTO.title,
            content = reportRequestDTO.content,
            requirement = reportRequestDTO.requirement,
            objects = reportRequestDTO.objects,
            exampleIO = reportRequestDTO.exampleIO,
            reportType = reportRequestDTO.reportType,
            startAt = reportRequestDTO.startAt.toInstant(),
            endAt = reportRequestDTO.endAt.toInstant(),
            level = reportRequestDTO.level
        )
        return reportRepository.save(report)
    }

    suspend fun getAllReport(): Flux<Report>{
        return reportRepository.findAll()
    }

    // Report 업데이트
    suspend fun updateReport(id: String, reportRequestDTO: ReportRequestDTO): Mono<Report> {
        return reportRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "리포트를 찾을 수 없습니다: $id")))
            .flatMap { existingReport ->
                val updatedReport = Report(
                    id = id,
                    week = reportRequestDTO.week,
                    seq = reportRequestDTO.seq,
                    title = reportRequestDTO.title,
                    content = reportRequestDTO.content,
                    requirement = reportRequestDTO.requirement,
                    objects = reportRequestDTO.objects,
                    exampleIO = reportRequestDTO.exampleIO,
                    reportType = reportRequestDTO.reportType,
                    startAt = reportRequestDTO.startAt.toInstant(),
                    endAt = reportRequestDTO.endAt.toInstant(),
                    level = reportRequestDTO.level
                )
                reportRepository.save(updatedReport)
            }
    }

    // Report 삭제
    suspend fun deleteReport(id: String): Mono<String> {
        return reportRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "리포트를 찾을 수 없습니다: $id")))
            .flatMap { report ->
                reportRepository.delete(report).thenReturn("리포트 삭제 완료")
            }
    }

    // 진행 중인 리포트들의 요약 정보만 조회
    suspend fun getAllOngoingReportSummaries(): Flux<ReportSummaryDTO> {
        val now = Instant.now().atZone(ZoneId.of("UTC")).toInstant()

        return reportRepository.findAll()
            .filter { it.startAt.isBefore(now) }
            .map { report ->
                ReportSummaryDTO(
                    id = report.id ?: "",
                    seq = report.seq,
                    week = report.week,
                    title = report.title,
                    level = report.level,
                    reportType = report.reportType,
                    endAt = report.endAt
                )
            }
    }

    // 특정 ID의 Report 조회 (ReportDetailDTO 반환)
    suspend fun getReportDetailById(id: String): Mono<ReportDetailDTO> {
        return reportRepository.findById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "리포트를 찾을 수 없습니다: $id")))
            .map { report ->
                ReportDetailDTO(
                    id = report.id ?: "",
                    week = report.week,
                    title = report.title,
                    content = report.content,
                    requirement = report.requirement,
                    objects = report.objects,
                    exampleIo = report.exampleIO,
                    reportType = report.reportType,
                    endAt = report.endAt,
                    level = report.level
                )
            }
    }
}
