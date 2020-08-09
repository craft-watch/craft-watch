package watch.craft

const val CANONICAL_BRANCH = "master"

val ciBranch: String? = System.getenv("CIRCLE_BRANCH")

val runningOnCi = System.getenv("CI") != null

val runningOnCanonicalCiBranch = runningOnCi && ciBranch == CANONICAL_BRANCH
