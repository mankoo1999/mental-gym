package com.mentalgym.app.domain.model

fun ExerciseInteractionKind.usesInteractivePanel(): Boolean =
    this != ExerciseInteractionKind.OPEN_PRACTICE
