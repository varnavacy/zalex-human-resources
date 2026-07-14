package com.kyriakos.compose.project.demo.zalexhumanresources.controller;

import com.kyriakos.compose.project.demo.zalexhumanresources.dto.EmployeeCertificationDTO;
import com.kyriakos.compose.project.demo.zalexhumanresources.model.Status;
import com.kyriakos.compose.project.demo.zalexhumanresources.service.CertificationRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebMvcTest(CertificationRequestController.class)
public class CertificationRequestControllerTest {

    public static final String ADDRESS_TO = "address_to";
    public static final String HR_DEPARTMENT = "HR Department";
    public static final String PROOF_OF_EMPLOYMENT = "Proof of employment";
    public static final String EMPLOYEE_ID = "employee_id";
    public static final String EMPLOYEE_ID_PARAM = "employeeId";
    public static final String PURPOSE = "purpose";
    public static final String STATUS = "status";
    public static final String ADDRESS_TO_PARAM = "addressTo";
    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private CertificationRequestService certificationRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCertificationRequest_success()  {
        Map<String, String> requestBody = Map.of(
                ADDRESS_TO, HR_DEPARTMENT,
                PURPOSE, PROOF_OF_EMPLOYMENT,
                EMPLOYEE_ID, "1"
        );

        EmployeeCertificationDTO response = new EmployeeCertificationDTO(
                HR_DEPARTMENT, PROOF_OF_EMPLOYMENT, new Date(), 1L, Status.OPEN.name()
        );

        when(certificationRequestService.createCertificationRequest(any()))
                .thenReturn(response);

        assertThat(mockMvc.post().uri("/certification-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.address_to", v -> assertThat(v).asString().isEqualTo(HR_DEPARTMENT))
                .hasPathSatisfying("$.purpose", v -> assertThat(v).asString().isEqualTo(PROOF_OF_EMPLOYMENT))
                .hasPathSatisfying("$.status", v -> assertThat(v).asString().isEqualTo(Status.OPEN.name()));
    }

    @Test
    void createCertificationRequest_missingPurpose_throwsBadRequest()  {
        when(certificationRequestService.createCertificationRequest(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purpose field is required"));

        Map<String, String> requestBody = Map.of(
                ADDRESS_TO, HR_DEPARTMENT,
                EMPLOYEE_ID, "1"
        );

        assertThat(mockMvc.post().uri("/certification-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getEmployeeCertifications_success()  {
        EmployeeCertificationDTO dto = new EmployeeCertificationDTO(
                HR_DEPARTMENT, PROOF_OF_EMPLOYMENT, new Date(), 1L, "OPEN"
        );

        Page<EmployeeCertificationDTO> mockPage = new PageImpl<>(List.of(dto));

        when(certificationRequestService.getEmployeeCertifications(any(), anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        assertThat(mockMvc.get().uri("/certification-requests")
                .param(EMPLOYEE_ID_PARAM, "1"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$[0].address_to", v -> assertThat(v).asString().isEqualTo(HR_DEPARTMENT))
                .hasPathSatisfying("$[0].status", v -> assertThat(v).asString().isEqualTo("OPEN"))
                .hasPathSatisfying("$[0].address_to", v -> assertThat(v).asString().isEqualTo(HR_DEPARTMENT));
    }

    @Test
    void getEmployeeCertifications_filterByStatus_returnsMatchingResults()  {
        EmployeeCertificationDTO dto = new EmployeeCertificationDTO(
                HR_DEPARTMENT, PROOF_OF_EMPLOYMENT, new Date(), 1L, Status.OPEN.name()
        );

        when(certificationRequestService.getEmployeeCertifications(any(), anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        assertThat(mockMvc.get().uri("/certification-requests")
                .param(EMPLOYEE_ID_PARAM, "1")
                .param(STATUS, Status.OPEN.name()))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$[0].status", v -> assertThat(v).asString().isEqualTo(Status.OPEN.name()));
    }

    @Test
    void getEmployeeCertifications_filterByReferenceNo_returnsMatchingResult()  {
        EmployeeCertificationDTO dto = new EmployeeCertificationDTO(
                HR_DEPARTMENT, PROOF_OF_EMPLOYMENT, new Date(), 5L, Status.OPEN.name()
        );

        when(certificationRequestService.getEmployeeCertifications(any(), anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        assertThat(mockMvc.get().uri("/certification-requests")
                .param(EMPLOYEE_ID_PARAM, "1")
                .param("referenceNo", "5"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$[0].reference_no", v -> assertThat(v).isEqualTo(5));
    }

    @Test
    void getEmployeeCertifications_filterByAddressTo_returnsMatchingResults()  {
        EmployeeCertificationDTO dto = new EmployeeCertificationDTO(
                "Embassy of Neptune", PROOF_OF_EMPLOYMENT, new Date(), 1L, Status.OPEN.name()
        );

        when(certificationRequestService.getEmployeeCertifications(any(), anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        assertThat(mockMvc.get().uri("/certification-requests")
                .param(EMPLOYEE_ID_PARAM, "1")
                .param(ADDRESS_TO_PARAM, "Embassy"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$[0].address_to", v -> assertThat(v).asString().isEqualTo("Embassy of Neptune"));
    }

    @Test
    void getEmployeeCertifications_invalidEmployeeId_throwsBadRequest()  {
        when(certificationRequestService.getEmployeeCertifications(any(), anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee id field is required"));

        assertThat(mockMvc.get().uri("/certification-requests")
                .param(EMPLOYEE_ID_PARAM, "0"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
