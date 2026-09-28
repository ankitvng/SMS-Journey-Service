package com.vonage.smsjourney;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        // Import all classes except those from third-party libraries and tests
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(
                        "com.vonage.smsjourney"
                );
    }

    // ====== LAYERED ARCHITECTURE TEST ======
    @Test
    void testLayeredArchitecture() {
        Architectures.LayeredArchitecture layeredArchitecture = Architectures.layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Adapter").definedBy("..adapter..")

                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter")
                .whereLayer("Adapter").mayNotBeAccessedByAnyLayer();

        layeredArchitecture.check(importedClasses);
    }

    // ====== DOMAIN MODULE RESTRICTIONS ======
    @Test
    void domainShouldNotDependOnOtherModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..",
                        "..adapter.."
                );

        rule.check(importedClasses);
    }

    @Test
    void domainShouldNotUseExternalFrameworks() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "io.micronaut..",
                        "jakarta.inject..",
                        "jakarta.persistence..",
                        "javax.persistence..",
                        "org.hibernate..",
                        "co.elastic..",
                        "com.fasterxml.jackson.."
                );

        rule.check(importedClasses);
    }

    // ====== APPLICATION MODULE RESTRICTIONS ======
    @Test
    void applicationShouldOnlyDependOnDomainAndCommon() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..adapter..");

        rule.check(importedClasses);
    }

    // ====== ADAPTER MODULE RESTRICTIONS ======
    @Test
    void adaptersShouldNotBeAccessedByDomainOrApplication() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..");

        rule.check(importedClasses);
    }

    @Test
    void adapterClassesImplementingPortsShouldBeNamedCorrectly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..adapter..")
                .and().implement(interfacesNamed(".*Repository", ".*Service", ".*Port"))
                .and().haveNameNotMatching(".*\\$.*Intercepted") // Exclude Micronaut-generated intercepted classes
                .should().haveNameMatching(".*Adapter")
                .orShould().haveNameMatching(".*Impl");

        rule.check(importedClasses);
    }

    @Test
    void adaptersShouldNotDependOnOtherAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapter..")
                .should(notDependOnOtherAdapters());

        rule.check(importedClasses);
    }

    // ====== GENERAL CODING RULES ======
    @Test
    void noFieldInjection() {
        NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(importedClasses);
    }

    // Helper method for interface name matching
    private static InterfacesNamed interfacesNamed(String... patterns) {
        return new InterfacesNamed(patterns);
    }

    // Custom condition for matching interfaces by name pattern
    private static class InterfacesNamed extends DescribedPredicate<JavaClass> {
        private final String[] patterns;

        InterfacesNamed(String... patterns) {
            super("interfaces named matching " + String.join(", ", patterns));
            this.patterns = patterns;
        }

        @Override
        public boolean test(JavaClass input) {
            if (!input.isInterface()) {
                return false;
            }
            for (String pattern : patterns) {
                if (input.getSimpleName().matches(pattern)) {
                    return true;
                }
            }
            return false;

        }
    }

    private ArchCondition<JavaClass> notDependOnOtherAdapters() {
        return new ArchCondition<>("depend on classes from another adapter") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                clazz.getDirectDependenciesFromSelf().forEach(dep -> checkDependency(dep, clazz, events));
            }

            private void checkDependency(Dependency dep, JavaClass clazz, ConditionEvents events) {
                String thisAdapter = getAdapterName(clazz.getPackageName());
                String targetAdapter = getAdapterName(dep.getTargetClass().getPackageName());

                if (targetAdapter != null
                        && !targetAdapter.equals(thisAdapter)) {
                    String message = String.format(
                            "Class %s (adapter '%s') depends on %s (adapter '%s')",
                            clazz.getName(), thisAdapter,
                            dep.getTargetClass().getName(), targetAdapter
                    );
                    events.add(SimpleConditionEvent.violated(clazz, message));
                }
            }
        };
    }

    private String getAdapterName(String pkg) {
        // Extracts the segment after "adapter"
        String[] parts = pkg.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("adapter") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null;
    }

}
