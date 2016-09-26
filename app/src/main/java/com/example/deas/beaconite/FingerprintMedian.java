package com.example.deas.beaconite;

import org.altbeacon.beacon.Beacon;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements that a fingerprint as calculated as a median of the given data.
 * <p>
 * Created by deas on 16/09/16.
 */
public class FingerprintMedian extends Fingerprint {
	private Map<Beacon, BeaconFingerPrint> beacons = new HashMap<>();

	public FingerprintMedian(BeaconMap allBeacons, List<TimeInterval> timeIntervals) {
		super(allBeacons, timeIntervals);
	}

	/**
	 * Traverses all Beacons, given via the constructor, and generates a fingerprint for each
	 * beacon. Then stores a beacon-fingerprint pair in an internal data structure. A
	 * general/big/cache Fingerprint here therefor consists of a list of beacons, where each beacon
	 * has a BeaconFingerPrint.
	 */
	@Override
	protected void calculateFingerprint() {
		for (Beacon b : allBeacons.keySet()) {
			BeaconFingerPrint bfp = new BeaconFingerPrint(allBeacons.rssisForTimeintervals(b,
					timeIntervals));
			beacons.put(b, bfp);
		}
	}

	/**
	 * Generates a fingerprint for an individual Beacon.
	 */
	class BeaconFingerPrint {
		// default values for invisible Beacons
		// -200 because -100 is the lowest possible Rssi value (very far away)
		final Integer INVISIBLE = -200;
		private Integer median = INVISIBLE;
		private double upperLimit = INVISIBLE;
		private double lowerLimit = INVISIBLE;

		/**
		 * Takes a list of Integer rssi values. Sorts the list, if not empty, and calculates a
		 * Median for the values in the list as well as a corridor.
		 *
		 * @param rssis
		 */
		public BeaconFingerPrint(List<Integer> rssis) {
			if (!rssis.isEmpty()) {
				Collections.sort(rssis);
				calcMedian(rssis);
				calcCorridor(rssis);
			}
		}

		/**
		 * A corridor for given rssi values. The corridor represents the margin of deviation from
		 * the calculated Median for these rssi values.
		 *
		 * @param rssis
		 */
		private void calcCorridor(List<Integer> rssis) {
			Long upperSum = 0L;
			int upperN = 0;

			Long lowerSum = 0L;
			int lowerN = 0;

			for (Integer rssi : rssis) {
				Long distance = rssi.longValue() - median;
				if (distance > 0) {
					upperSum += distance * distance;
					upperN++;
				} else if (distance < 0) {
					lowerSum += distance * distance;
					lowerN++;
				}
			}

			upperLimit = median + Math.sqrt(upperSum) / upperN;
			lowerLimit = median - Math.sqrt(lowerSum) / lowerN;
		}

		private void calcMedian(List<Integer> rssis) {
			this.median = rssis.get(rssis.size() / 2);
		}

		/**
		 * Returns if given rssi value is in the boundaries, i.e. the calculated corridor, of this
		 * BeaconFingerPrint.
		 *
		 * @param rssi the rssi value to check if it is in this beacons fingerprint.
		 * @return true if the rssi value is in the boundaries/corridor of this beacons fingerprint;
		 * false if not.
		 */
		boolean isCovered(Integer rssi) {
			return (rssi >= lowerLimit) && (rssi <= upperLimit);
		}

	}
}
