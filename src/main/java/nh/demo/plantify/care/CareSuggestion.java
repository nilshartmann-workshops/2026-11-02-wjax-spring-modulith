package nh.demo.plantify.care;

import nh.demo.plantify.shared.CareTaskType;

import java.time.LocalDate;

public sealed interface CareSuggestion
    permits CareSuggestion.OneTimeCareSuggestion, CareSuggestion.RecurringCareSuggestion {

    CareTaskType taskType();
    int confidence();

    static CareSuggestion oneTime(CareTaskType taskType, int confidence, LocalDate dueDate) {
        return new OneTimeCareSuggestion(taskType, confidence, dueDate);
    }

    static CareSuggestion recurring(CareTaskType taskType, int confidence, int intervalDays) {
        return new RecurringCareSuggestion(taskType, confidence, intervalDays);
    }

    // Would be better to have thos default implementation in their own
    // files, but for demo purposes it's easier to have them here
    record OneTimeCareSuggestion(
        CareTaskType taskType,
        int confidence,
        LocalDate dueDate
    ) implements CareSuggestion {
    }

    record RecurringCareSuggestion(
        CareTaskType taskType,
        int confidence,
        int intervalDays
    ) implements CareSuggestion {
    }
}