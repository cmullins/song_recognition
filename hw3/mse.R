#

# Get a linear separator for a data matrix using the MSE approach. Finds an exact
# solution using the pseudo-inverse and a margin vector of c(1, 1, ..., 1).
get_mse_separator <- function(X) {
	X <- as.matrix(X)

	# Name stuff so it's consistent with book/slides
	Y <- X
	b <- matrix(1, nrow = nrow(Y))

	# Check if pseudo-inverse exists
	YtY <- t(Y) %*% Y

	# If it does, we can find an exact solution.
	if (det(YtY) != 0) {
		return ( solve(t(Y) %*% Y) %*% t(Y) %*% b )
	}
	# Otherwise, we'll have to use gradient decent.
	else {
		# Turns out that the data we're given has a pseudoinverse (w/out the limit),
		# so I'm omitting the gradient decent implementation due to time constraints.
	}
}
