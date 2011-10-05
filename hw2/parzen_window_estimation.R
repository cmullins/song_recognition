### parzen_window_estimation.R
###
### Defines functions for creating Parzen window estimations for a given dataset.

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

# Given a matrix of data, return a 0-mean Gaussian PDF with a diagonal covariance
# matrix.
get_gaussian_window_fn <- function(data) {
	sigma     <- diag( sd(data) )
	sigma_inv <- solve(sigma) 
	const     <- 1 / ((2*pi)^(ncol(data) / 2) * sqrt(det(sigma)))

	function(X) {
		const * exp( -(1/2) * drop(t(X) %*% sigma_inv %*% X))
	}
}

# Get the delta function that simplifies the PDF we're after by assimilating the 
# volume and height calculations.
get_delta_fn <- function(data, window_width) {
	h      <- window_width
	n_dim  <- ncol(data)
	V_inv  <- 1/((window_width)^(n_dim))
	phi_fn <- get_gaussian_window_fn(data)

	function(X) {
		v <- V_inv * phi_fn(X / window_width)

		if (v > 1) {
#			browser()
		}

		v
	}
}

# Given prepared data, returns an "object" containing all the things necessary to
# do Parzen window estimation.
get_parzen_window_pdf_fn <- function(data_, window_width_) {
	data <- data_
	h    <- window_width_

	delta_fn <- get_delta_fn(data, h)

	function(X) {
		n     <- nrow(data)
		sum   <- 0
		diffs <- matrix(X, nrow = nrow(data), ncol = ncol(data), byrow = TRUE) - data

		for (i in 1:n) {
			if (delta_fn(diffs[i,]) > 1) {
				print(sprintf("IT HAPPENED (%f): ", delta_fn(diffs[i,])))
				print(diffs[i,])
			}
			sum <- sum + delta_fn(diffs[i,])
		}

		(sum / n)
	}
}
