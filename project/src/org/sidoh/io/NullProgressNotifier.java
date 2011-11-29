package org.sidoh.io;

public class NullProgressNotifier extends ProgressNotifier {
	
	protected static class NullProgressNotifierBuilder extends ProgressNotifier.Builder {
		@Override
		public ProgressNotifier create(String message, int maxValue) {
			return new NullProgressNotifier();
		}
	}

	public NullProgressNotifier() {
		super("",0);
	}

	@Override
	protected void render(int value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void complete() {
		// TODO Auto-generated method stub
	}

}
