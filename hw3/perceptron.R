### perceptron.R
###
### Perceptron learning.

# Learning rate function that always returns the same thing.
const_learning_rate_fn <- function(val) {
	function(i) {
		val
	}
}

# Gets a linear separator using the perceptron algorithm.  Returns a vector 
# of weights for each of the input dimensions, plus one weight for a bias 
# (constant) term.
#
# WARNING: This assumes that the input data is linearly separable! It will 
# run forever if it's not!
#
# X
#     data matrix. first <n> columns should be the data. The last column
#     should be the class (either -1 or 1) for this datum.
# learning_rate_fn
#     Function that defines how the learning rate is updated. It should
#     accept a single parameter: the epoch, and return the learning rate
#     to be used.
# epoch_callback
#     If specified, this function will be called at each epoch. It will
#     be provided two parameters: the linear separator determined so far,
#     and whether or not this is the final answer that will be returned
#     (as a boolean).
get_perceptron <- function(X, learning_rate_fn, epoch_callback = NULL) {
	X <- as.matrix(X)
	n <- ncol(X)-1

	# Initialize stuff.
	a <- t(as.matrix(runif(n, min(X), max(X))))

	i <- 0
	eta <- learning_rate_fn(i)
	misclass <- TRUE

	# Keep going as long as there are misclassified items.
	while (misclass) {
		misclass <- FALSE

		# Accumulate all changes and update them at the end of the epoch.
		delta <- matrix(0, ncol = n)

		# Consider each sample in the input, and check if it's misclassified.
		for (j in 1:nrow(X)) {
			val <- a %*% X[j,1:n]

			if (sign(val) != sign(X[j,n+1])) {
				misclass <- TRUE

				# Update linear function
				delta <- delta + X[j,1:n]*X[j,n+1]
			}
		}

		# Update vector
		if (misclass) {
			a <- a + eta*delta
		}

		i <- i + 1
		eta <- learning_rate_fn(i)

		# Notify callback of new epoch (if there's a callback to speak of)
		if (! is.null(epoch_callback)) {
			epoch_callback(a, !misclass)
		}
	}

	return (a)
}
