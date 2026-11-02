package nh.demo.plantify.rose;

import nh.demo.plantify.care.suggestions.CareSuggestion;
import nh.demo.plantify.care.suggestions.CareSuggestionFactory;
import nh.demo.plantify.care.suggestions.CareSuggestionService;
import nh.demo.plantify.plant.PlantType;
import nh.demo.plantify.shared.CareTaskType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class RoseCareSuggestionFactory implements CareSuggestionFactory {

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