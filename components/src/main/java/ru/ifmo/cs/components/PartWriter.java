/*
 * $Id$
 */

package ru.ifmo.cs.components;

/**
 *
 * @author Dmitry Afanasiev <KOT@MATPOCKuH.Ru>
 */
public class PartWriter extends Component implements DataDestination {
	private final DataPart dst;
	private final long startbit;

	public PartWriter(DataPart dst, long startbit, long width) {
		super(width);

		this.dst = dst;
		this.startbit = startbit;
	}

	@Override
	public synchronized void setValue(long value) {
		dst.setValue(value, startbit, mask);
	}
}
