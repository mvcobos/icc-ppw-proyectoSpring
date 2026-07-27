package ec.edu.ups.icc.proyecto.events.dtos;

import java.time.OffsetDateTime;

import ec.edu.ups.icc.proyecto.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.proyecto.events.entities.EventModality;
import ec.edu.ups.icc.proyecto.events.entities.EventStatus;
import ec.edu.ups.icc.proyecto.users.dtos.UserResponseDto;

public class EventResponseDto {

    private Long id;
    private String title;
    private String description;
    private EventModality modality;
    private String location;
    private String virtualUrl;
    private Integer capacity;
    private Integer availableCapacity;
    private OffsetDateTime registrationStartAt;
    private OffsetDateTime registrationEndAt;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private EventStatus status;
    private UserResponseDto organizer;
    private CategoryResponseDto category;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Constructor vacío
    public EventResponseDto() {
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventModality getModality() {
        return modality;
    }

    public void setModality(EventModality modality) {
        this.modality = modality;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVirtualUrl() {
        return virtualUrl;
    }

    public void setVirtualUrl(String virtualUrl) {
        this.virtualUrl = virtualUrl;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }

    public OffsetDateTime getRegistrationStartAt() {
        return registrationStartAt;
    }

    public void setRegistrationStartAt(OffsetDateTime registrationStartAt) {
        this.registrationStartAt = registrationStartAt;
    }

    public OffsetDateTime getRegistrationEndAt() {
        return registrationEndAt;
    }

    public void setRegistrationEndAt(OffsetDateTime registrationEndAt) {
        this.registrationEndAt = registrationEndAt;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public UserResponseDto getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserResponseDto organizer) {
        this.organizer = organizer;
    }

    public CategoryResponseDto getCategory() {
        return category;
    }

    public void setCategory(CategoryResponseDto category) {
        this.category = category;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}