### common.R
###
### Methods that might be useful in other homeworks/projects are put here.

# Get a Gaussian PDF with the provided mean and covariance matrix. This should
# work for an arbitrary number of dimensions. Note that the dimensions for the
# mean and the covariance matrix should match. In addition, the covariance
# matrix cannot be singular.
get_gaussian_pdf <- function(mean, sigma) {
	mean      <- as.vector(mean)
	# Need inverse of sigma for calculation
	sigma     <- as.matrix(sigma)
	sigma_inv <- solve(sigma)

	# The number of dimensions
	k         <- nrow(sigma)

	# Pre-compute the constant term.
	const     <- 1 / ( ((2*pi)^(k/2)) * det(sigma)^(1/2) )

	function(X) {
		diff <- X - mean
		const * exp( (-1/2) * drop(t(diff) %*% sigma_inv %*% diff) )
	}
}

# Samples data and splits it into test/train data.
train_test_split <- function(data, sample_rate) {
	n  <- nrow(data)
	ix <- sample(1:n, round(sample_rate * n))

	list(
		train = data[ix,],
		test  = data[-ix,])
}

# Why doesn't R have printf when it does have sprintf? So goofy...
printf <- function(format, ...) {
	cat(sprintf(format, ...), "\n")
}
