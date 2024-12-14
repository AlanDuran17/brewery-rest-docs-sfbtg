package guru.springframework.msscbrewery.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.msscbrewery.ConstrainedFields;
import guru.springframework.msscbrewery.services.BeerService;
import guru.springframework.msscbrewery.services.CustomerService;
import guru.springframework.msscbrewery.web.model.BeerDto;
import guru.springframework.msscbrewery.web.model.CustomerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "alan.duran.dev", uriPort = 443)
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @MockBean
    CustomerService customerService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    CustomerDto customerDto;

    @BeforeEach
    public void setUp() {
        customerDto = CustomerDto.builder()
                .id(UUID.randomUUID())
                .name("Michael Scott")
                .build();
    }

    @Test
    void getCustomer() throws Exception {
        given(customerService.getCustomerById(any(UUID.class))).willReturn(customerDto);

        mockMvc.perform(get("/api/v1/customer/{customerId}", customerDto.getId().toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(customerDto.getId().toString())))
                .andExpect(jsonPath("$.name", is("Michael Scott")))
                .andDo(document("v1/customer-get",
                        pathParameters (
                                parameterWithName("customerId").description("UUID of desired customer to get.")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Id of customer"),
                                fieldWithPath("name").description("Name of the customer")
                        )
                ));
    }

    @Test
    void handlePost() throws Exception {
        CustomerDto customerDto1 = customerDto;
        customerDto1.setId(null);
        CustomerDto savedDto = CustomerDto.builder().id(UUID.randomUUID()).name("Dwight Schrute").build();

        String customerDtoJson = objectMapper.writeValueAsString(customerDto1);

        given(customerService.saveNewCustomer(any())).willReturn(savedDto);
        ConstrainedFields fields = new ConstrainedFields(CustomerDto.class);

        mockMvc.perform(post("/api/v1/customer/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerDtoJson))
                .andExpect(status().isCreated())
                .andDo(document("v1/customer-save",
                        requestFields(
                                fields.withPath("id").ignored(),
                                fields.withPath("name").description("Name of the customer").attributes()
                        )));
    }

    @Test
    void handleUpdate() throws Exception {
        CustomerDto customerDto1 = customerDto;
        customerDto1.setId(null);
        CustomerDto savedDto = CustomerDto.builder().id(UUID.randomUUID()).name("Dwight Schrute").build();

        String customerDtoJson = objectMapper.writeValueAsString(customerDto1);

        given(customerService.saveNewCustomer(any())).willReturn(savedDto);
        ConstrainedFields fields = new ConstrainedFields(CustomerDto.class);

        mockMvc.perform(put("/api/v1/customer/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerDtoJson))
                .andExpect(status().isNoContent())
                .andDo(document("v1/customer-update",
                        requestFields(
                                fields.withPath("id").ignored(),
                                fields.withPath("name").description("Name of the customer").attributes()
                        )));

        then(customerService).should().updateCustomer(any(), any());
    }

    @Test
    void deleteById() throws Exception {
        given(customerService.getCustomerById(any(UUID.class))).willReturn(customerDto);

        mockMvc.perform(delete("/api/v1/customer/{customerId}", customerDto.getId().toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("v1/customer-delete",
                        pathParameters (
                                parameterWithName("customerId").description("UUID of desired customer to get.")
                        )
                ));
    }
}