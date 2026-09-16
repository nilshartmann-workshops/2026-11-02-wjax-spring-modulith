package nh.demo.plantify.plant;

import java.util.UUID;

public record PlantRegisteredEvent(UUID plantId, UUID ownerId, String name, PlantType plantType, String location) {
}
