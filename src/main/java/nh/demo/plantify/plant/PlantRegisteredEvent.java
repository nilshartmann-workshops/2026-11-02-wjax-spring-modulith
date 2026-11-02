package nh.demo.plantify.plant;

import org.springframework.modulith.events.Externalized;

import java.util.UUID;

public record PlantRegisteredEvent(
    UUID plantId,
    UUID ownerId,
    String name,
    PlantType plantType,
    String location) {
}