### hw3.R
###
### Seamlessly runs all of the tasks required for HW3.

# Settings specified in the report
################################################################################
settings <- list(
	# Filename of data to use.
	DATA_FILE = "./hw3_data.txt",

	# Color palette to use for the plots
	COLORS = c('#ff0000', '#0000ff'),

	# Plot characters to use for the plots
	PCH = c(2, 3),

	# Number of repetitiosn to do in the perceptron section. If this is changed,
	# PERCEPTRON_PLOT_MFROW should also be changed (this affects the number of
	# plots that can be crammed into one file).
	NUM_PERCEPTRON_REPETITIONS = 4,

	# Number of rows and columns (respectively) that should be crammed into one
	# file for the Perceptron plots
	PERCEPTRON_PLOT_MFROW = c(2,2),

	# Learning rates to try
	PERCEPTRON_LEARNING_RATES = c(0.01, 0.5)
)

# Helper methods
################################################################################

# Generates a ps plot
generate_plot <- function(filename,
	plot_fn,
	width = 6,
	height = 6) {

	palette(settings$COLORS)

	if (! file.exists('plots')) {
		dir.create('plots')
	}

	postscript(file = sprintf("plots/%s.eps", filename),
		bg = 'white',
		width = width,
		height = height,
		horizontal = FALSE,
		paper = 'special')

	plot_fn()

	dev.off()
}

# Plot data in a matrix with the specified title
plot_class_data <- function (data, title, legend = TRUE) {
	plot(NULL, 
		xlim = range(data[,1]), 
		ylim = range(data[,2]),
		main = title,
		xlab = "x",
		ylab = "y")
	lines(data[data[,3] == 1,],
		type = 'p',
		col = 1,
		pch = settings$PCH[1])
	lines(data[data[,3] == -1,],
		type = 'p',
		col = 2,
		pch = settings$PCH[2])
	if (legend) {
		legend("topleft",
			c("Class 1", "Class -1"),
			col = settings$COLORS,
			pch = settings$PCH)
	}
}

# Adds a column for a bias (constant) term and inverts the sign of the negative
# class.
prepare_data_perceptron <- function(X) {
	n <- ncol(X)

	# Introduce bias 
	X <- cbind(X[,1:n-1], 1, X[,n])
}

# Prepares data for MSE. Inverts the sign of the negative class.
prepare_data_mse <- function(X) {
	n <- ncol(X)-1

	cbind( (X[,1:n] * X[,n+1]), X[,n+1] )
}

# Begin HW
################################################################################

source('mse.R')
source('perceptron.R')

# Load the data
data <- as.matrix(read.table(settings$DATA_FILE))

data_perceptron <- prepare_data_perceptron(data)
data_mse <- prepare_data_mse(data)

# Plot the data
generate_plot('raw_data',
	function() { plot_class_data(data, '') })

# PROBLEM 1. Perceptron criterion function
#  (a) Initialize solution vector randomly (done in perceptron.R)
#  (b) Use batch mode and plot the decision boundary after each epoch on top of
#  the scatterplot of the training samples. Indicate the class that each sample
#  belongs to.
#  (c) Try learning rates of 0.01 and 0.5. Report how the learning behavior
#  changes.

# Runs perceptron experiments for a single learning rate
plot_perceptron <- function(learning_rate_fn, title) {
	old <- par()$mfrow
	par(mfrow = settings$PERCEPTRON_PLOT_MFROW)

	# Callback function that will plot the separator at each epoch
	callback_fn <- function(a, final) {
		x <- floor(min(data[,1])):ceiling(max(data[,1]))
		y <- (-a[3] - a[1]*x)/a[2]
		c <- ''

		if (final) {
			c <- 'green'
		}
		else {
			c <- 'black'
		}

		lines(x, y, col = c)
	}

	for (i in 1:settings$NUM_PERCEPTRON_REPETITIONS) {
		# Plot the data
		plot_class_data(data, title, FALSE)

		# Find linear separator. The callback_fn does all of the work. We don't
		# actually want to use the boundary for anything... just plotting.
		get_perceptron(data_perceptron, learning_rate_fn, callback_fn)
	}

	par(mfrow = old)
}

for (rate in settings$PERCEPTRON_LEARNING_RATE) {
	eta_k <- const_learning_rate_fn(rate)
	fn    <- function() { 
		plot_perceptron(eta_k, '')
	}

	generate_plot(sprintf("perceptron_%.2f", rate), fn)
}

# PROBLEM 2. MSE learning procedure
# (a) Assume that the components b_i of the margin vector are all 1.
# (b) Check whether the pseudoinverse of Y exists or not. If it exists, use the exact
# solution for the solution vector. Otherwise, apply gradient descent methods to find
# a solution. In the latter case, set the learning rate to 0.5.
# (c) Plot the decision boundary on top of the scatter plot of the training samples.
# Indicate the class that each sample belongs to.

generate_plot('mse',
function() {
	a <- get_mse_separator(data_mse)

	# Plot class data
	plot_class_data(data, '', FALSE)

	# Plot separator
	x <- floor(min(data[,1])):ceiling(max(data[,1]))
	y <- (-a[3] - a[1]*x)/a[2]
	lines(x,y,col='green')
})
