package org.example.shared.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "CitizenRequest")
public class CitizenRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "requestId", length = 36, columnDefinition = "VARCHAR(36)")
    private UUID requestId;
    private String fullName;
    private String nationalId;
    private int requestNumber;
    private LocalDateTime requestDate;
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "departmentId", length = 36, columnDefinition = "VARCHAR(36)")
    private UUID departmentId;
    private int processStatus;

    public CitizenRequest() {

    }

    public CitizenRequest(String fullName, String nationalId, LocalDateTime requestDate, UUID departmentId) {
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.requestDate = requestDate;
        this.departmentId = departmentId;
        this.processStatus = 0;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(int requestNumber) {
        this.requestNumber = requestNumber;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public int getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(int processStatus) {
        this.processStatus = processStatus;
    }
}
