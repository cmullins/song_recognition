#

write_plot <- function(file, plot_fn) {
	png(filename = sprintf("%s.png", file),
		width = 800,
		height = 800)

	plot_fn()

	dev.off()
}

# Returns the accuracy of the results matrix. Note that this assumes the answer
# threshold is 1 - meaning that any answer is good enough to be considered 
# viable.
get_raw_accuracy <- function(X) {
	right <- nrow(X[X[,ncol(X)] == 1,])

	right / nrow(X)
}

# Measures the accuracy of the provided data when any confidence value <= the
# maximum confidence value of an incorrect answer is counted as incorrect.
get_accuracy_wrong_answer_corrected <- function(X) {
	max_incorrect <- X[X[,ncol(X)]==0,5]
	if (is.null(max_incorrect) || length(max_incorrect) == 0) {
		max_incorrect <- 0
	}
	else {
		max_incorrect <- max(max_incorrect)
	}

	raw_correct   <- X[X[,ncol(X)]==1,]
	no_wrongs_correct <- raw_correct[raw_correct[,5]>max_incorrect,]

	nrow(no_wrongs_correct) / nrow(X)
}

# Get number of misclassified results.
get_num_misclassified <- function(X) {
	wrong <- nrow(X[X[,ncol(X)] == 0,])
}

# Returns a single-row matrix with the first value being the threshold, the
# second value being the accuracy at this threshold, and the third being the 
# rate of misclassified answers.
get_threshold_reading <- function(X, threshold) {
	passing <- X[X[,5] >= threshold,]

	right <- nrow(passing[passing[,ncol(X)] == 1,])
	accuracy <- right / nrow(X)

	wrong <- nrow(passing[passing[,ncol(X)] == 0,])
	wrong_rate <- wrong / nrow(X)

	matrix(c(threshold, accuracy, wrong_rate), ncol = 3)
}

file <- NULL
args <- commandArgs()
for (i in 1:length(args)) {
	if (args[i] == "--args") {
		file <- args[i+1]
		break;
	}
}

data <- read.table(file, sep = ",")

#### PRINT RAW STATS
cat("RAW RESULTS ======================== \n")

cat(sprintf("%40s: %d\n", "Total no. of results", nrow(data)))
cat(sprintf("%40s: %d\n", "No. misclassified results", get_num_misclassified(data)))
cat(sprintf("%40s: %.5f\n", "Raw accuracy", get_raw_accuracy(data)))
cat(sprintf("%40s: %.5f\n", "No wrong answer accuracy", get_accuracy_wrong_answer_corrected(data)))

#### PRINT PER-LENGTH RESULTS
cat("PER CLIP LENGTH ==================== \n")

lens <- sort(unique(data[,3]))

for (len in lens) {
	len_data <- data[data[,3] == len,]

	cat(sprintf("%3d seconds\n", len))

	cat(sprintf("      no misclassified = %d\n", get_num_misclassified(len_data)))
	cat(sprintf("      accuracy         = %.5f\n", get_raw_accuracy(len_data)))
}

#### PRINT PER-NOISE RESULTS
cat("PER NOISE ========================== \n")

noises <- unique(data[,4])

for (noise in noises) {
	noise_data <- data[data[,4] == noise,]

	cat(sprintf("%10s\n", noise))

	cat(sprintf("%10s no. misclassified = %d\n", "", get_num_misclassified(noise_data)))
	cat(sprintf("%10s accuracy          = %.5f\n", "", get_raw_accuracy(noise_data)))
}

#### ACCURACY MATRIX
cat("ACCURACY MATRIX ============================================= \n")
first_col_len <- max(apply(matrix(noises), 1, nchar)) + 1
matrix_len    <- (first_col_len+4) + 11*length(lens)
row_sep       <- paste(c("+", rep("-",matrix_len-2), "+"),sep="")

cat(row_sep, "\n", sep="")
cat(sprintf(
	paste("| %", first_col_len, "s |", sep = ""),
	""))
for (i in 1:length(lens)) {
	cat(sprintf("    %2d    |", lens[[i]]))
}
cat("\n")
cat(row_sep, "\n", sep="")

for (noise in noises) {
	cat(sprintf(
		paste("| %", first_col_len, "s |", sep=""),
		noise))

	for (len in lens) {
		noise_data <- data[data[,4]==noise,]
		final_data <- noise_data[noise_data[,3]==len,]

		cat(sprintf("  %.4f  |", 
			get_raw_accuracy(final_data)))
	}

	cat("\n")
}
cat(row_sep, "\n", sep="")

#### NO WRONG ANSWERS ACCURACY
cat("NO WRONG ANSWERS ACCURACY MATRIX =================================== \n")
first_col_len <- max(apply(matrix(noises), 1, nchar)) + 1
matrix_len    <- (first_col_len+4) + 11*length(lens)
row_sep       <- paste(c("+", rep("-",matrix_len-2), "+"),sep="")

cat(row_sep, "\n", sep="")
cat(sprintf(
	paste("| %", first_col_len, "s |", sep = ""),
	""))
for (i in 1:length(lens)) {
	cat(sprintf("    %2d    |", lens[[i]]))
}
cat("\n")
cat(row_sep, "\n", sep="")

for (noise in noises) {
	cat(sprintf(
		paste("| %", first_col_len, "s |", sep=""),
		noise))

	for (len in lens) {
		noise_data <- data[data[,4]==noise,]
		final_data <- noise_data[noise_data[,3]==len,]

		cat(sprintf("  %.4f  |", 
			get_accuracy_wrong_answer_corrected(final_data)))
	}

	cat("\n")
}
cat(row_sep, "\n", sep="")


#### Generate threshold graph

thresholds      <- sort(unique(data[,5]))
threshold_data  <- NULL

for(threshold in thresholds) {
	threshold_data <- rbind(threshold_data, get_threshold_reading(data, threshold))
}

write_plot("accuracy_vs_wrong_answer_tradeoff",
	function() {
		plot(threshold_data[,2], threshold_data[,3],
			main = "Accuracy vs. Wrong answer tradeoff",
			xlab = "Accuracy",
			ylab = "Wrong answer rate",
			type = 'l')
	})
