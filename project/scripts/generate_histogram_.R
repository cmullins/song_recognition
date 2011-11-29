# 

write_plot <- function(filename, plot_fn) {
	png(filename = sprintf("%s.png", filename),
		width = 1600,
		height = 400)

	plot_fn()

	dev.off()
}

file <- NULL
args <- commandArgs()
for (i in 1:length(args)) {
    if (args[i] == "--args") {
        file <- args[i+1]
        break;
    }
}

data <- read.table(file)
write_plot(file, function() {
	barplot(data[,2])
})
