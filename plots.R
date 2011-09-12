### plots.R
###
### Defines functions to create pretty plots and stuff.

source('discriminant_analysis.R')

# Settings
settings <- list(
	# Colors for the background in the plots
	bg_palette = c("#C4F5E4", "#F2C7C7", "#BAC2E3"),
	
	# Colors for the foreground in the plots
	fg_palette = c("#007700", "#770000", "#000077"),

	# Resolution / brush size for decision regions
	dr_resolution = 0.01, dr_brush_size = 1)

# Routines to generate plots

create_scatter_plot <- function(prepared_data, d_fns) {
	orig_cols <- palette() # save palette

	# Set up the canvas
	prepare_scatter_plot(prepared_data)

	# Paint decision regions
	palette(settings$bg_palette) # use bg colors
	paint_decision_regions(d_fns, 
		prepared_data,
		settings$dr_resolution,
		settings$dr_brush_size)
	
	# Plot points / legend
	palette(settings$fg_palette) # use fg colors
	plot_categories(prepared_data)

	palette(orig_cols) # restore palette
}

# Sets up the canvas
prepare_scatter_plot <- function(prepared_data) {
	plot(c(),
		xlim = range(prepared_data$raw_data[,1]),
		ylim = range(prepared_data$raw_data[,2]),
		col  = palette(),
		xlab = "x",
		ylab = "y")
}

# Places the data points from the prepared data onto the canvas.
plot_categories <- function(prepared_data) {
	for (i in 1:prepared_data$num_classes) {
		# Plot the points
		lines(prepared_data$classes[[i]],
			type = 'p',
			pch  = '•',
			col  = i,
			cex  = 1.5)

		# Plot the mean
		lines(
			x    = mean(prepared_data$classes[[i]][,1]),
			y    = mean(prepared_data$classes[[i]][,2]),
			type = 'p',
			pch  = 8, # a square
			col  = i,
			cex  = 3) # a big square
	}

	# Create the legend
	legend("bottomright",
		c("1", "2", "3"),
		col = 1:3,
		pch = 15,
		title = "Classes")
}

# Plots classify results. Assumes that classify was called on a prepared_data
# instance. I don't think I'm going to use this because it's really ugly.
plot_results <- function(classify_results) {
	for (i in 1:classify_results$prepared_data$num_classes) {
		# Plot each point in the raw data, use the classify results for color
		lines(classify_results$prepared_data$raw_data,
			type = 'p',
			pch  = 0,
			cex  = 1.2,
			col  = classify_results$results[,3])
	}

	# Create the legend
	legend("bottomleft",
		c("1", "2", "3"),
		col = 1:3,
		pch = 0,
		title = "Results")
}

# Paints the decision regions given the discriminant functions. Assumes the input
# data was 2D. The resolution argument specifies the granularity at which the 
# discriminant functions should be run on. The brush_size argument specifies how
# big the points plotted should be (passed to lines as cex)
paint_decision_regions <- function(disc_functions, prepared_data, 
resolution = 0.01,
brush_size = 2) {
	# Prepare a matrix of points that should be evaluated
	x_min <- min(prepared_data$raw_data[,1])
	x_max <- max(prepared_data$raw_data[,1])
	x_pts <- seq(x_min-1, x_max+2, by = resolution)

	y_min <- min(prepared_data$raw_data[,2])
	y_max <- max(prepared_data$raw_data[,2])
	y_pts <- seq(y_min-1, y_max+1, by = resolution)

	pts <- expand.grid(x_pts, y_pts)

	# Evaluate each row in the matrix
	r <- classify(disc_functions, raw_data = pts)

	# Paint the regions
	lines(pts,
		type = 'p',
		pch  = 15,
		col  = r$results[,3],
		cex  = brush_size)
}
