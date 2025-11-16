import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.registering
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

extensions.configure(JacocoPluginExtension::class) {
    toolVersion = libs.findVersion("jacoco").get().requiredVersion
}

tasks.withType<Test>().configureEach {
    extensions.configure(JacocoTaskExtension::class.java) {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val jacocoTestReport by tasks.registering(JacocoReport::class) {
    dependsOn("testReleaseUnitTest")
    reports {
        xml.required = true
        html.required = true
    }
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/*\$.*",
        "**/ui/components/**",
        "**/ui/theme/**",
        "**/ui/DemoActivity*.*",
        "**/ui/CurrencyListFragment*.*",
        "**/CurrencyDemoApp*.*",
        "**/di/DatabaseModule*.*",
        "**/di/RepositoryModule*.*",
        "**/data/local/CurrencyDatabase*.*"
    )
    val kotlinReleaseTree = fileTree("${buildDir}/tmp/kotlin-classes/release") {
        exclude(fileFilter)
    }
    val javaReleaseTree = fileTree("${buildDir}/intermediates/javac/release/classes") {
        exclude(fileFilter)
    }
    classDirectories.setFrom(files(kotlinReleaseTree, javaReleaseTree))
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "jacoco/testReleaseUnitTest.exec",
                "outputs/unit_test_code_coverage/releaseUnitTest/testReleaseUnitTest.exec",
                "outputs/code_coverage/**/*.ec"
            )
        }
    )
}

tasks.named("check").configure {
    dependsOn(jacocoTestReport)
}

val updateCoverageSummary by tasks.registering {
    mustRunAfter(jacocoTestReport)
    doLast {
        val reportFile = file("$buildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        if (!reportFile.exists()) {
            logger.warn("Jacoco XML report not found at ${reportFile.absolutePath}")
            return@doLast
        }
        val documentBuilder = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            val featureFlags = mapOf(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd" to false,
                "http://xml.org/sax/features/external-general-entities" to false,
                "http://xml.org/sax/features/external-parameter-entities" to false,
                "http://apache.org/xml/features/disallow-doctype-decl" to false
            )
            featureFlags.forEach { (feature, enabled) ->
                try {
                    setFeature(feature, enabled)
                } catch (_: Exception) {
                    // Some XML parsers might not support the feature; ignore and continue.
                }
            }
        }.newDocumentBuilder()
        val document = documentBuilder.parse(reportFile)
        val nodes = document.getElementsByTagName("counter")
        var covered = 0.0
        var missed = 0.0
        for (index in 0 until nodes.length) {
            val node = nodes.item(index)
            if (
                node is org.w3c.dom.Element &&
                node.getAttribute("type") == "LINE" &&
                node.parentNode != null &&
                node.parentNode.nodeName == "report"
            ) {
                covered = node.getAttribute("covered").toDouble()
                missed = node.getAttribute("missed").toDouble()
                break
            }
        }
        val total = covered + missed
        if (total == 0.0) {
            println("Jacoco line coverage: 0% (no executable code)")
            return@doLast
        }
        val percent = covered / total * 100.0
        val formatted = String.format(Locale.US, "%.2f", percent)
        println("Jacoco line coverage: $formatted%")

        val readmeFile = rootProject.file("README.md")
        if (!readmeFile.exists()) {
            logger.warn("README.md not found in project root; skipping coverage write")
            return@doLast
        }
        val markerStart = "<!-- COVERAGE:START -->"
        val markerEnd = "<!-- COVERAGE:END -->"
        val regex = Regex(
            pattern = "${Regex.escape(markerStart)}(.*?)${Regex.escape(markerEnd)}",
            options = setOf(RegexOption.DOT_MATCHES_ALL)
        )
        val readmeText = readmeFile.readText()
        var updated = if (regex.containsMatchIn(readmeText)) {
            regex.replace(readmeText) { "$markerStart$formatted%$markerEnd" }
        } else {
            readmeText
        }
        val badgeLineRegex = Regex(
            pattern = """^!\[Coverage\]\(https://img\.shields\.io/badge/coverage-[0-9.]+%25-brightgreen\).*$""",
            options = setOf(RegexOption.MULTILINE)
        )
        if (badgeLineRegex.containsMatchIn(updated)) {
            val badgeValue = formatted.replace("%", "%25")
            updated = badgeLineRegex.replace(updated) {
                "![Coverage](https://img.shields.io/badge/coverage-$badgeValue%25-brightgreen)"
            }
        }
        readmeFile.writeText(updated)
    }
}

jacocoTestReport.configure {
    finalizedBy(updateCoverageSummary)
}
