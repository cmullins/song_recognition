### hw2.R
###
### Script that seamlessly runs all of the tasks that we're asked to do for
### HW2.

source('common.R')
source('kernel_density_estimation.R')

# Settings specified in the report
################################################################################
settings <- list(
	# Data to use for this exerpiment.
	DATA_FILE = 'iris.txt',

	# Rate to sample the available data with. This fraction of the data will
	# be used for training; 1 - this will be used for testing.
	SAMPLE_RATE = (2/3),

	# Window widths to try
	WINDOW_WIDTHS = c(0.01, 0.5, 10),

	# Kernel function to use.
	# get_diagonal_variance_gaussian_kernel_fn gets a gaussian kernel function
	# where the covariance matrix is diagonal with the entries being the
	# variance of each dimension in the sample.
	KERNEL_FN_BUILDER = get_diagonal_variance_gaussian_kernel_fn,

	# Number of times to repeat the experiment
	NUM_REPETITIONS = 15,
	
	# use 'ascii' for an ASCII table, use 'tex' for a LaTeX table.
	OUTPUT_FORMAT = 'ascii')
################################################################################

# Helper methods
################################################################################

# Loads data from a file and prepares it for use in the rest of the methods. 
# Assumes that the last column in the data indicates the class of the row its
# in.
prepare_data <- function(file, sep = ',') {
	raw_data <- read.table(file, sep = sep)

	classes <- unique(raw_data[, ncol(raw_data)])
	by_class <- list()

	for (class in classes) {
		by_class[[class]] <- as.matrix(raw_data[raw_data[,ncol(raw_data)] == class,1:(ncol(raw_data)-1)])
	}

	list(
		classes = classes,
		raw_data = raw_data,
		by_class = by_class)
}

# Obvious classifier
classify <- function(X, pdfs) {
	choice <- 1
	choice_val <- pdfs[[1]](X)
	
	for (i in 2:length(pdfs)) {
		val <- pdfs[[i]](X)

		if (val > choice_val) {
			choice <- i
			choice_val <- val
		}
	}

	choice
}

run_experiment <- function(h, kernel_fn) {
	train <- list()
	test  <- NULL
	pdfs  <- list()

	# For each class, split its data into train/test data, build a PDF 
	# estimation from the train data and apply it to the test data.
	for (class in data$classes) {
		split <- train_test_split(data$by_class[[class]], settings$SAMPLE_RATE)

		train[[class]] <- split$train
		test           <- rbind(test, cbind(split$test, class))
		pdfs[[class]]  <- get_kernel_density_fn(split$train, h, kernel_fn)
	}

	# Strip the class column from the test data before we attempt to classify.
	test_vectors <- test[,1:(ncol(test) - 1)]

	# Classify the whole thing
	classify_fn <- function(X) {
		classify(X, pdfs)
	}
	answers <- as.vector( apply(test_vectors, 1, classify_fn) )

	# Compute and return the error rate
	n_total <- nrow(test_vectors)
	n_right <- nrow(test[test[,ncol(test)] == answers,])

	( (n_total - n_right) / n_total )
}
################################################################################

# Read/parse data
cat("Reading data...\n")
data <- prepare_data(settings$DATA_FILE)

# Run the experiment
results <- NULL
for (run in 1:settings$NUM_REPETITIONS) {
	run_results <- NULL

	for (h in settings$WINDOW_WIDTHS) {
		r <- run_experiment(h, settings$KERNEL_FN_BUILDER)

		run_results <- c(run_results, r)
	}

	results <- rbind(results, run_results)
	
	printf("Done with run %d!", run)
}

means <- apply(results, 2, mean)
variances <- sd(results)^2

# Report results in the requested format.
if (settings$OUTPUT_FORMAT == 'ascii') {
	cat("================================================================================\n")
	cat(sprintf("%20s", "RUN"))
	for (h in settings$WINDOW_WIDTHS) {
		cat(sprintf("%20s", sprintf("WIDTH = %.3f", h)))
	}
	cat("\n")
	cat('--------------------------------------------------------------------------------\n')

	for (i in 1:nrow(results)) {
		cat(sprintf("%20d", i))

		for (k in 1:ncol(results)) {
			cat(sprintf("%20s", sprintf("%f", results[i, k])))
		}

		cat("\n")
	}
	cat('--------------------------------------------------------------------------------\n')

	
	cat(sprintf("%20s", "MEAN"))
	for (k in 1:ncol(results)) {
		cat(sprintf("%20s", sprintf("%f", means[k])))
	}

	cat('\n')

	cat(sprintf("%20s", "VARIANCE"))
	for (k in 1:ncol(results)) {
		cat(sprintf("%20s", sprintf("%f", variances[k])))
	}

	cat('\n')
	cat("================================================================================\n")
} else if (settings$OUTPUT_FORMAT == 'tex') {
	cat("\\begin{tabular}{|c", rep('|c', length(settings$WINDOW_WIDTHS)), "|}\n", sep = '')
	cat("\\hline\n")
	cat("{\\bf Run} ")
	for (i in settings$WINDOW_WIDTHS) {
		cat(sprintf("& {\\bf Width = %.3f} ", i))
	}
	cat("\\\\ \n")
	cat("\\hline\n")
	
	for (i in 1:nrow(results)) {
		cat(i)
		
		for (k in 1:ncol(results)) {
			cat(sprintf(" & %f", results[i, k]))
		}
		
		cat("\\\\ \n")
	}
	
	cat("\\hline \n")
	
	cat("{\\bf Mean}")
	for (k in 1:ncol(results)) {
		cat(sprintf(" & %f", means[k]))
	}
	cat("\\\\ \n")
	
	cat("{\\bf Variance} ")
	for (k in 1:ncol(results)) {
		cat(sprintf(" & %f", variances[k]))
	}
	cat("\\\\ \n")
	cat("\\hline \n")
	
	cat("\\end{tabular}")
}
