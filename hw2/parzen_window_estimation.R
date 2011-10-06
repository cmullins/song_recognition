### parzen_window_estimation.R
###
### Defines functions for creating Parzen window estimations for a given dataset.

source('common.R')

# Gets a gaussian kernel function for use in parzen window pdf estimator. It
# assumes you want the variance to be 1.
get_unit_variance_gaussian_kernel_fn <- function(data) {
	k     <- ncol(data)
	mu    <- matrix(0, ncol = k, nrow = 1)
	sigma <- diag(k)

	get_gaussian_pdf(mu, sigma)
}

# Gets a gaussian kernel function where the covariance matrix is I*sigma^2,
# where sigma^2 is the mean of the variance across all dimensions of the
# data
get_mean_variance_gaussian_kernel_fn <- function(data) {
	k      <- ncol(data)
	mu     <- matrix(0, ncol = k, nrow = 1)
	sigma  <- diag(k) * mean( sd(data) )

	get_gaussian_pdf(mu, sigma)
}

# Gets a gaussian kernel function where the covariance matrix is a 
# diagonal matrix. Sigma_ii = variance for dimension i in the data.
get_diagonal_variance_gaussian_kernel_fn <- function(data) {
	k      <- ncol(data)
	mu     <- matrix(0, ncol = k, nrow = 1)
	sigma  <- diag( sd(data) )

	get_gaussian_pdf(mu, sigma)
}

# Get a gaussian kernel function where Sigma = cov(data). If cov(data) is
# singular, this won't work. A possible workaround might be to add some
# very small random values to data.
get_gaussian_kernel_fn <- function(data) {
	k      <- ncol(data)
	mu     <- matrix(0, ncol = k, nrow = 1)
	sigma  <- cov(data)

	get_gaussian_pdf(mu, sigma)
}

# The naive kernel function that returns 1 if the point is in a hypercube
# with unit length centered at the orgin, and 0 otherwise.
get_delta_kernel_fn <- function(data) {
	function(X) {
		if (length(X[abs(X) <= (1/2)]) == length(X)) {
			return(1)
		}
		else {
			return(0)
		}
	}
}

# Get a PDF estimator using the Parzen window estimation technique. The third
# argument should be a function that returns a kernel function to use (e.g., a
# Gaussian). Said function will be passed the data as the first (and only
# parameter).
get_parzen_window_pdf <- function(data, h, window_fn_builder) {
	# Number of dimensions in the data
	k <- ncol(data)
	# Number of vectors in the sample
	n <- nrow(data)
	# Volume of the k-dimensional hypercube.
	V <- (h^k)
	# Get the window function, which can be constructed from the argument
	# window_fn_builder
	phi <- window_fn_builder(data)

	function(X) {
		# Compute the diff for each vector with a matrix operation. It ends up
		# being faster this way.
		sum  <- sum( apply(data, 1, function(xi) { phi((X - xi) / h) }) )

		# Divide by the volme after the fact. This prevents buffer underflow in
		# some cases.
		sum / (n * V)
	}
}
