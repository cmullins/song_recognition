### discriminant_analysis.R
###
### Defines functions that create linear/quadratic discriminant functions given
### a matrix of data with arbitrary dimensionality.

# Given a data matrix, split it up into classes. Assumes that the data is sorted
# by class, and that there are the same number of elements per class.
prepare_data <- function(X, num_classes = 3) {
	classes <- list()
	inc     <- nrow(X) / num_classes
	id      <- 1

	for (start in seq(1, nrow(X), by = inc)) {
		classes[[id]] <- X[start:(start+inc-1),]
		id <- id + 1
	}

	list(
		raw_data = X,
		num_classes = num_classes,
		classes = classes)
}

# Creates a generic linear discriminant function given:
#
#   1. A mean (matrix containing one value for each dimension)
#   2. A covariance matrix
#   3. Prior probability
#
# The returned value is a function g_i(X), which takes in a
# matrix where the rows are vectors to be scored.
make_disc_function <- function(mean_, sigma_, prior = 1) {
	l_mean <- mean_
	l_sigma <- sigma_
	
	# Get sigma inverse
	sigma_inv <- solve(l_sigma) 

	# Pre-compute constant terms
	const_t <- -0.5*log(det(sigma_inv)) + log(prior)

	function (X, progress = FALSE) {
		X      <- as.matrix(X)
		scores <- matrix(0, nrow = nrow(X))
		mu     <- matrix(l_mean, nrow = nrow(X), ncol = ncol(X), byrow = TRUE)
		diff   <- X - mu
		bsize  <- (nrow(X) / 10)

		t1 <- diff %*% sigma_inv

		# It'd be nice to do this as a matrix operation, but I don't see how to do that
		# without ending up with an N x N matrix when we only need N values. One could
		# just do the matrix multiplication by t(diff), and take the diagonal entries,
		# but this results in very poor performance. Probably best to just do this one
		# iteratively.
		for (i in 1:nrow(X)) {
			v <- -0.5*drop(t1[i,] %*% diff[i,])
			v <- v + const_t

			scores[i,1] <- v

			if (progress == TRUE && (i %% bsize) == 0) {
				cat( sprintf(" %d%% ", i/nrow(X)*100) )
			}
		}
		if (progress == TRUE) {
			cat( "\n" )
		}

		scores #return
	}
}

# Given data prepared by prepare_data(X,num_classes), create case 1 discriminant
# functions for each of the classes. Case 1 discriminant functions are where the
# covariance matrix for each of the n classes is assumed to be:
#
#       (sigma^2) * I_n
# 
# Assumes equal prior probabilities.
create_case1_disc_fns <- function(prepared_data) {
	sigma <- diag(ncol(prepared_data$raw_data)) * mean(sd(prepared_data$raw_data))
	sigma_fn <- function(class) {
		sigma
	}

	create_disc_fns(prepared_data, sigma_fn)
}

# Creates case 2 discriminant functions. This is where the covariance matrix for
# each of the classes is:
#
#       cov(data)
create_case2_disc_fns <- function(prepared_data) {
	sigma <- cov(prepared_data$raw_data)
	sigma_fn <- function(class) {
		sigma
	}

	create_disc_fns(prepared_data, sigma_fn)
}

# Creates case 3 discriminant functions. This is where the covariance matrix for
# each of the classes is the covariance matrix for the data itself.
create_case3_disc_fns <- function(prepared_data) {
	sigma_fn <- function(class) {
		cov(class)
	}

	create_disc_fns(prepared_data, sigma_fn)
}

# Creates a list of discriminant functions for each of the classes in the 
# prepared data. sigma_fn is a function called on each class that is used to
# determine the sigma parameter for make_disc_function(mean,sigma,prior)
create_disc_fns <- function(prepared_data, sigma_fn) {
	d_fns <- list()

	for (i in 1:prepared_data$num_classes) {
		d          <- prepared_data$classes[[i]]
		d_fns[[i]] <- make_disc_function(mean(d), sigma_fn(d))
	}

	d_fns #return
}

# Classifies each row in data by annotation the index of the discriminant function
# that returns the greatest value.
classify <- function(disc_functions, prepared_data = NULL, raw_data = NULL, progress = FALSE) {
	if (missing(raw_data)) {
		data <- prepared_data$raw_data
	} else {
		data <- raw_data
	}

	p_print <- function(msg) {
		if (progress == TRUE) {
			cat(msg)
		}
	}

	p_print("  | d_1:")
	disc_values <- matrix(disc_functions[[1]](data, progress = progress), ncol = 1)

	for (index in 2:length(disc_functions)) {
		p_print(sprintf("  | d_%i:", index))
		disc_values <- cbind(disc_values, disc_functions[[index]](data, progress = progress))
	}

	list(prepared_data = prepared_data,
		results = cbind(data, max.col(disc_values)))
}

# Measures the accuracy of a results matrix. Assumes that the class is given by:
#   ceiling(index/nrows(X))
get_accuracy <- function(classify_results) {
	n_classes <- classify_results$prepared_data$num_classes
	n_rows    <- nrow(classify_results$prepared_data$raw_data)

	sum(classify_results$results[,3]
		== ceiling((n_classes * 1:n_rows)/n_rows)) / n_rows
}
