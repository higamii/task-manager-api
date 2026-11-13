package com.higamii.taskmanager.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_BODY = """
            {"title": "Estudar Spring Boot", "description": "Ver JPA", "priority": "HIGH"}
            """;

    private int createTask() throws Exception {
        String json = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    @Test
    @DisplayName("POST /api/tasks cria tarefa e retorna 201 com Location")
    void shouldCreateTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.title").value("Estudar Spring Boot"));
    }

    @Test
    @DisplayName("POST /api/tasks com dados inválidos retorna 400")
    void shouldReturn400ForInvalidBody() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.priority").exists());
    }

    @Test
    @DisplayName("GET /api/tasks/{id} inexistente retorna 404")
    void shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tasks lista tarefas paginadas")
    void shouldListTasks() throws Exception {
        createTask();

        mockMvc.perform(get("/api/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} atualiza a tarefa")
    void shouldUpdateTask() throws Exception {
        int id = createTask();

        mockMvc.perform(put("/api/tasks/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Novo título\", \"priority\": \"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Novo título"))
                .andExpect(jsonPath("$.priority").value("LOW"));
    }

    @Test
    @DisplayName("Tarefa concluída não pode ser reaberta (409)")
    void shouldNotReopenDoneTask() throws Exception {
        int id = createTask();

        mockMvc.perform(patch("/api/tasks/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(patch("/api/tasks/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"TODO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} remove a tarefa")
    void shouldDeleteTask() throws Exception {
        int id = createTask();

        mockMvc.perform(delete("/api/tasks/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tasks/summary retorna contagem por status")
    void shouldReturnSummary() throws Exception {
        createTask();

        mockMvc.perform(get("/api/tasks/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todo").value(1))
                .andExpect(jsonPath("$.total").value(1));
    }
}
