### hw2.R
###
### Script that seamlessly runs all of the tasks that we're asked to do for
### HW2.

source('parzen_window_estimation.R')

# Settings specified in the report
settings <- list(
	# Rate to sample the available data with. This fraction of the data will
	# be used for training; 1 - this will be used for testing.
	SAMPLE_RATE = (2/3),

	# Window widths to try
	WINDOW_WIDTHS = c(0.001, 0.01, 0.5, 10.0),
	#WINDOW_WIDTHS = c(10),

	# Number of times to repeat the experiment
	NUM_REPETITIONS = 15)

printf <- function(format, ...) {
	cat(sprintf(format, ...), "\n")
}

# Samples data and splits it into test/train data. 
train_test_split <- function(data, sample_rate) {
	n  <- nrow(data)
	ix <- sample(1:n, round(sample_rate * n))

	list(
		train = data[ix,],
		test  = data[-ix,])
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

data <- prepare_data('iris.txt')

# Sample each class 

# Run the experiment
results <- NULL
for (run in 1:settings$NUM_REPETITIONS) {
	printf("Doing run %d...", run)

	w_results <- NULL
	for (h in settings$WINDOW_WIDTHS) {
		printf("---> Window width = %f", h)
		# Sample the data
		train_data <- list()
		test_data  <- NULL
		pdfs       <- list()
		
		for (class in data$classes) {
			split_data <- train_test_split(data$by_class[[class]], settings$SAMPLE_RATE)
		
			train_data[[class]] <- split_data$train
			pdfs[[class]]       <- get_parzen_window_pdf_fn(split_data$train, h)
			
			if (is.null(test_data)) {
				test_data <- cbind(split_data$test, class)
			}
			else {
				test_data <- rbind(test_data, cbind(split_data$test, class))
			}
		}

		r <- apply(test_data[,1:(ncol(test_data)-1)], 1, function(X) { classify(X, pdfs) })
		correct <- nrow(test_data[test_data[,ncol(test_data)] == as.vector(r),])
		n       <- nrow(test_data)
		error   <- ((n - correct) / n)
		
		w_results <- c(w_results, error)
	}

	results <- rbind(results, c(run, w_results))
}

print(results)
apply(results, 2, mean)
sd(results)
