package com.spendsense.group.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spendsense.group.dto.CreateGroupRequest;
import com.spendsense.group.dto.GroupResponse;
import com.spendsense.group.dto.UpdateGroupRequest;
import com.spendsense.group.entity.GroupType;
import com.spendsense.group.service.GroupService;
import com.spendsense.security.CustomUserDetailsService;
import com.spendsense.security.JwtAuthenticationFilter;
import com.spendsense.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GroupControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void shouldCreateGroupSuccessfully() throws Exception {

        CreateGroupRequest request =
                new CreateGroupRequest(
                        "Family",
                        "Family expenses",
                        GroupType.FAMILY,
                        "#2196F3",
                        "home",
                        true,
                        false,
                        true
                );

        GroupResponse response =
                new GroupResponse(
                        UUID.randomUUID(),
                        "Family",
                        "Family expenses",
                        GroupType.FAMILY,
                        "#2196F3",
                        "home",
                        false,
                        UUID.randomUUID(),
                        "Swathi",
                        true,
                        false,
                        true,
                        LocalDateTime.now()
                );

        when(groupService.createGroup(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/groups")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Family"))
                .andExpect(jsonPath("$.groupType").value("FAMILY"))
                .andExpect(jsonPath("$.createdByName").value("Swathi"))
                .andExpect(jsonPath("$.budgetEnabled").value(true));

        verify(groupService).createGroup(any());
    }

    @Test
    void shouldGetMyGroupsSuccessfully() throws Exception {

        List<GroupResponse> groups = List.of(
                new GroupResponse(
                        UUID.randomUUID(),
                        "Family",
                        "Family expenses",
                        GroupType.FAMILY,
                        "#2196F3",
                        "home",
                        false,
                        UUID.randomUUID(),
                        "Swathi",
                        true,
                        false,
                        true,
                        LocalDateTime.now()
                )
        );

        when(groupService.getMyGroups())
                .thenReturn(groups);

        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Family"));

        verify(groupService).getMyGroups();
    }

    @Test
    void shouldGetGroupByIdSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();

        GroupResponse response =
                new GroupResponse(
                        groupId,
                        "Family",
                        "Family expenses",
                        GroupType.FAMILY,
                        "#2196F3",
                        "home",
                        false,
                        UUID.randomUUID(),
                        "Swathi",
                        true,
                        false,
                        true,
                        LocalDateTime.now()
                );

        when(groupService.getGroupById(groupId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/groups/{groupId}", groupId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Family"));

        verify(groupService).getGroupById(groupId);
    }

    @Test
    void shouldUpdateGroupSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();

        UpdateGroupRequest request =
                new UpdateGroupRequest(
                        "Family Updated",
                        "Updated Description",
                        "#000000",
                        "group"
                );

        GroupResponse response =
                new GroupResponse(
                        groupId,
                        "Family Updated",
                        "Updated Description",
                        GroupType.FAMILY,
                        "#000000",
                        "group",
                        false,
                        UUID.randomUUID(),
                        "Swathi",
                        true,
                        false,
                        true,
                        LocalDateTime.now()
                );

        when(groupService.updateGroup(
                eq(groupId),
                any(UpdateGroupRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/groups/{groupId}", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Family Updated"));

        verify(groupService)
                .updateGroup(eq(groupId), any(UpdateGroupRequest.class));
    }

    @Test
    void shouldArchiveGroupSuccessfully() throws Exception {

        UUID groupId = UUID.randomUUID();

        doNothing()
                .when(groupService)
                .archiveGroup(groupId);

        mockMvc.perform(
                        delete("/api/v1/groups/{groupId}", groupId)
                )
                .andExpect(status().isNoContent());

        verify(groupService)
                .archiveGroup(groupId);
    }
}
