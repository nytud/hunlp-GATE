#!/usr/bin/perl -w

use strict;
use warnings;
use POSIX;

use POSIX "locale_h";
setlocale ( LC_ALL, "hu_HU" );
use locale;

use File::Basename;
use Getopt::Std;
my $PROG = basename ( $0 );

my %opt;
getopts ( "hd", \%opt ) or usage();
usage() if $opt{h}; # or ...

my $DEBUG = ( defined $opt{d} ? 1 : 0 );

# --- program starts HERE
while (<>) {
  chomp;
  my $l = $_;

  my @a = split /\t/, $l;

  my @p = ();
  push @p, $a[6] if $a[6] ne '--'; # eszköz neve
  push @p, $a[8] if $a[8] ne '--'; # kódolás
  push @p, $a[9] if $a[9] ne '--'; # technológia

  my $p = join ', ', @p;

  my $s = '';

  $s .= $a[5];                             # nyelv (=HU)
  $s .= " $a[2]" if $a[2] ne '--';         # címke
  $s .= " $a[3]." if $a[3] ne '--';        # futtatási "sorszám"
  $s .= " \\\"$a[7]\\\"" if $a[7] ne '--'; # em* név
  $s .= " $a[4]" if $a[4] ne '--';         # mi az?
  $s .= " ($p)" if $p;
  $s .= " [$a[10]]" if $a[10] ne '--';     # oprsz

  print "$a[0]\n";                         # java fájl, aminek a neve :)
  print "$s\n";
  print "\n";
}

# --- subs

# prints usage info
sub usage {
  print STDERR <<USAGE;

Usage: $PROG [-d] [-h]
Creates canonical \@CreoleResource names for infra2 GATE PRs. :)
  -d  turns on debugging
  -h  prints this help message & exit
Report bugs to <joker\@nytud.hu>.
USAGE

  exit 1;
}

