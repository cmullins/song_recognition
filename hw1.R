### hw1.R
###
### Runs the required tasks for HW1
###

########
# Utility functions

m_print <- function(msg) {
	cat(msg, "\n")
}

generate_plot <- function(filename,
plot_fn,
width = 800,
height = 600) {
	if (! file.exists('plots')) {
		dir.create('plots')
	}

	png(file = sprintf("plots/%s", filename),
		bg = 'white',
		width = width,
		height = height,
		units = 'px')

	plot_fn()

	dev.off()
}

#######
# Begin HW

source('discriminant_analysis.R')
source('plots.R')

m_print("Reading data...")
train_data <- read.table('hw1_traindata.txt')
test_data  <- read.table('hw1_testdata.txt')
######

m_print("Training...")
pd_train <- prepare_data(train_data)
pd_test  <- prepare_data(test_data)

case1_fn <- create_case1_disc_fns(pd_train)
case2_fn <- create_case2_disc_fns(pd_train)
case3_fn <- create_case3_disc_fns(pd_train)
case1_train_results <- classify(case1_fn, pd_train)
case2_train_results <- classify(case2_fn, pd_train)
case3_train_results <- classify(case3_fn, pd_train)
m_print("TRAIN ERROR:")
m_print(sprintf("\tCase 1 : %0.6f", 1-get_accuracy(case1_train_results)))
m_print(sprintf("\tCase 2 : %0.6f", 1-get_accuracy(case2_train_results)))
m_print(sprintf("\tCase 3 : %0.6f", 1-get_accuracy(case3_train_results)))
#######

m_print("Testing...")

case1_test_results <- classify(case1_fn, pd_test)
case2_test_results <- classify(case2_fn, pd_test)
case3_test_results <- classify(case3_fn, pd_test)

m_print("TEST ERROR:")
m_print(sprintf("\tCase 1 : %0.6f", 1-get_accuracy(case1_test_results)))
m_print(sprintf("\tCase 2 : %0.6f", 1-get_accuracy(case2_test_results)))
m_print(sprintf("\tCase 3 : %0.6f", 1-get_accuracy(case3_test_results)))
#######

m_print('Generating plots...')

generate_plot('case1.png',
	function() {
		create_scatter_plot(pd_train, case1_fn)
	})
generate_plot('case2.png',
	function() {
		create_scatter_plot(pd_train, case2_fn)
	})
generate_plot('case3.png',
	function() {
		create_scatter_plot(pd_train, case3_fn)
	})
