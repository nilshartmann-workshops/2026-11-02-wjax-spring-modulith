package nh.demo.plantify.rose;

import nh.demo.plantify.care.CareSuggestion;
import nh.demo.plantify.care.CareSuggestionFactory;
import nh.demo.plantify.owner.OwnerRepository;
import nh.demo.plantify.plant.PlantType;
import nh.demo.plantify.shared.CareTaskType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class RoseCareSuggestionFactory implements CareSuggestionFactory {

    // 🕵️‍♂️ Zeigen: OwnerRepository ist API von owner, der Architekturtest bleibt grün
    private final OwnerRepository ownerRepository;

    RoseCareSuggestionFactory(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public List<CareSuggestion> createSuggestion(PlantType plantType, String location) {
        if (plantType != PlantType.ROSES) {
            return List.of();
        }

        return List.of(
            CareSuggestion.recurring(CareTaskType.WATERING, 10, 3),
            CareSuggestion.recurring(CareTaskType.FERTILIZING, 10, 14),
            CareSuggestion.recurring(CareTaskType.PRUNING, 10, 30)
        );
    }
}
