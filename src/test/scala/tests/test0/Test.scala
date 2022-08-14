package tests.test0

import frontend.SpecReader
import org.junit.Test

/**
 * Tests well-formenedness conditions.
 */

class Test0 extends tests.UnitTest {
  setTest(0)

  @Test def test1(): Unit = {
    val spec = new SpecReader(getSpec(1))
    assert(spec.parserError == false)
    assert(spec.wfErrors.size == 25)
    // spec.wfErrors.generateOracle()
    spec.wfErrors.testOracle(
      (100,""),
      (110,"M1_StateNameUsedMoreThanOnce"),
      (190,"M2_StateIsNeverActivated"),
      (145,"M3_InitIsDefinedOnParameterizedState1Of2"),
      (145,"M4_InitIsDefinedOnParameterizedState2Of2"),
      (130,"M5_ModifierOccursMoreThanOnce"),
      (140,"M6_IncompatibleModifiers"),
      (140,"M6_IncompatibleModifiers"),
      (140,"M6_IncompatibleModifiers"),
      (140,"M6_IncompatibleModifiers"),
      (120,"M7_FormalParameterIdOccursMultipleTimes"),
      (150,"M8_StateHasNoTransitionsAndIsNotUsedInATransitionCondition"),
      (125,"M9_UnusedFormalParameters"),
      (210,"M10_MoreThanOneOccurrenceOfOkError"),
      (210,"M10_MoreThanOneOccurrenceOfOkError"),
      (220,"M11_OkMustOccurAloneOnRHS"),
      (200,"M12_ErrorIsNotAllowedOnLHS"),
      (200,"M13_OkIsNotAllowedOnLHS"),
      (170,"M13_OkIsNotAllowedOnLHS"),
      (170,"M14_TargetStateNotDefined"),
      (240,"M15_ReferenceToUndefinedNamesInPattern"),
      (250,"M16_ParameterNamesDoNotMatchWithState"),
      (230,"M16_ParameterNamesDoNotMatchWithState"),
      (225,"M17_ActualFieldNameOccursMultipleTimes"),
      (115,"M19_StateNamesMustDifferFromEventNames")
    )
  }
}
